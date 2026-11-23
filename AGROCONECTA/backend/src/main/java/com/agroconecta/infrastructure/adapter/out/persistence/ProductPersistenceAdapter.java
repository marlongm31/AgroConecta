package com.agroconecta.infrastructure.adapter.out.persistence;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.model.ProductCatalogItem;
import com.agroconecta.application.port.out.ProductRepositoryPort;
import com.agroconecta.domain.model.Product;
import com.agroconecta.domain.model.ProductCategory;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.ProductJpaEntity;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.PageMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.ProductJpaMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.repository.ProductJpaRepository;
import com.agroconecta.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository repository;
    private final UserJpaRepository userRepository;

    public ProductPersistenceAdapter(ProductJpaRepository repository, UserJpaRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(ProductJpaMapper::toDomain);
    }

    @Override
    public Product save(Product product) {
        return ProductJpaMapper.toDomain(repository.save(ProductJpaMapper.toEntity(product)));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public PageResult<Product> findByOwnerId(Long ownerId, PageQuery pageQuery) {
        PageRequest pageable = PageRequest.of(pageQuery.page(), pageQuery.size(),
                Sort.by("createdAt").descending());
        return PageMapper.toPageResult(repository.findByOwnerId(ownerId, pageable), ProductJpaMapper::toDomain);
    }

    @Override
    public PageResult<ProductCatalogItem> searchActive(String search, ProductCategory category, PageQuery pageQuery) {
        PageRequest pageable = PageRequest.of(pageQuery.page(), pageQuery.size(),
                Sort.by("createdAt").descending());
        Page<ProductJpaEntity> page;
        if (search == null && category == null) {
            page = repository.findByActiveTrueAndSoldOutFalse(pageable);
        } else if (search == null) {
            page = repository.findByActiveTrueAndSoldOutFalseAndCategory(category, pageable);
        } else if (category == null) {
            page = repository.searchActiveByText(search, pageable);
        } else {
            page = repository.searchActiveByTextAndCategory(search, category, pageable);
        }
        Map<Long, UserJpaEntity> producers = loadProducers(page.getContent());
        return PageMapper.toPageResult(page, entity -> toCatalogItem(entity, producers));
    }

    @Override
    public Optional<ProductCatalogItem> findActiveById(Long id) {
        return repository.findByIdAndActiveTrueAndSoldOutFalse(id).map(entity -> {
            Map<Long, UserJpaEntity> producers = loadProducers(List.of(entity));
            return toCatalogItem(entity, producers);
        });
    }

    @Override
    public long countActiveByOwnerId(Long ownerId) {
        return repository.countByOwnerIdAndActiveTrue(ownerId);
    }

    @Override
    public long countActive() {
        return repository.countByActiveTrueAndSoldOutFalse();
    }

    @Override
    public List<ProductCatalogItem> findRecentActive(int limit) {
        List<ProductJpaEntity> products = repository.findTop6ByActiveTrueAndSoldOutFalseOrderByCreatedAtDesc().stream()
                .limit(limit)
                .toList();
        Map<Long, UserJpaEntity> producers = loadProducers(products);
        return products.stream().map(entity -> toCatalogItem(entity, producers)).toList();
    }

    private Map<Long, UserJpaEntity> loadProducers(List<ProductJpaEntity> products) {
        List<Long> ownerIds = products.stream().map(ProductJpaEntity::getOwnerId).distinct().toList();
        return userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(UserJpaEntity::getId, Function.identity()));
    }

    private ProductCatalogItem toCatalogItem(ProductJpaEntity entity, Map<Long, UserJpaEntity> producers) {
        UserJpaEntity producer = producers.get(entity.getOwnerId());
        String producerName = producer != null ? producer.getFullName() : "Productor de AgroConecta";
        String producerLocation = producer != null ? producer.getLocation() : null;
        return new ProductCatalogItem(ProductJpaMapper.toDomain(entity), producerName, producerLocation);
    }
}
