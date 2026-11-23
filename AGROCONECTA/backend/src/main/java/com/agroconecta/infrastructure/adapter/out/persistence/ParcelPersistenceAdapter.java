package com.agroconecta.infrastructure.adapter.out.persistence;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.out.ParcelRepositoryPort;
import com.agroconecta.domain.model.Parcel;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.PageMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.ParcelJpaMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.repository.ParcelJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ParcelPersistenceAdapter implements ParcelRepositoryPort {

    private final ParcelJpaRepository repository;

    public ParcelPersistenceAdapter(ParcelJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Parcel> findById(Long id) {
        return repository.findById(id).map(ParcelJpaMapper::toDomain);
    }

    @Override
    public Parcel save(Parcel parcel) {
        return ParcelJpaMapper.toDomain(repository.save(ParcelJpaMapper.toEntity(parcel)));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public PageResult<Parcel> findByOwnerId(Long ownerId, PageQuery pageQuery) {
        PageRequest pageable = PageRequest.of(pageQuery.page(), pageQuery.size(),
                Sort.by("createdAt").descending());
        return PageMapper.toPageResult(repository.findByOwnerId(ownerId, pageable), ParcelJpaMapper::toDomain);
    }

    @Override
    public long countByOwnerId(Long ownerId) {
        return repository.countByOwnerId(ownerId);
    }

    @Override
    public boolean belongsToOwner(Long parcelId, Long ownerId) {
        return repository.existsByIdAndOwnerId(parcelId, ownerId);
    }
}
