package com.agroconecta.infrastructure.adapter.out.persistence;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.out.ProductionRepositoryPort;
import com.agroconecta.domain.model.ProductionRecord;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.PageMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.ProductionJpaMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.repository.ProductionJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProductionPersistenceAdapter implements ProductionRepositoryPort {

    private final ProductionJpaRepository repository;

    public ProductionPersistenceAdapter(ProductionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ProductionRecord> findById(Long id) {
        return repository.findById(id).map(ProductionJpaMapper::toDomain);
    }

    @Override
    public ProductionRecord save(ProductionRecord record) {
        return ProductionJpaMapper.toDomain(repository.save(ProductionJpaMapper.toEntity(record)));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public PageResult<ProductionRecord> findByOwnerId(Long ownerId, PageQuery pageQuery) {
        PageRequest pageable = PageRequest.of(pageQuery.page(), pageQuery.size(),
                Sort.by("harvestDate").descending());
        return PageMapper.toPageResult(repository.findByOwnerId(ownerId, pageable), ProductionJpaMapper::toDomain);
    }

    @Override
    public long countByOwnerId(Long ownerId) {
        return repository.countByOwnerId(ownerId);
    }
}
