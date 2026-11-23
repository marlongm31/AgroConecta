package com.agroconecta.infrastructure.adapter.out.persistence;

import com.agroconecta.application.port.out.ProductImageRepositoryPort;
import com.agroconecta.domain.model.ProductImage;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.ProductImageJpaMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.repository.ProductImageJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class ProductImagePersistenceAdapter implements ProductImageRepositoryPort {

    private final ProductImageJpaRepository repository;

    public ProductImagePersistenceAdapter(ProductImageJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ProductImage> findByProductId(Long productId) {
        return repository.findByProductIdOrderByPositionAscIdAsc(productId).stream()
                .map(ProductImageJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<ProductImage> findByProductIds(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return repository.findByProductIdInOrderByPositionAscIdAsc(productIds).stream()
                .map(ProductImageJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ProductImage> findById(Long imageId) {
        return repository.findById(imageId).map(ProductImageJpaMapper::toDomain);
    }

    @Override
    public long countByProductId(Long productId) {
        return repository.countByProductId(productId);
    }

    @Override
    public ProductImage save(ProductImage image) {
        return ProductImageJpaMapper.toDomain(repository.save(ProductImageJpaMapper.toEntity(image)));
    }

    @Override
    public void deleteById(Long imageId) {
        repository.deleteById(imageId);
    }
}
