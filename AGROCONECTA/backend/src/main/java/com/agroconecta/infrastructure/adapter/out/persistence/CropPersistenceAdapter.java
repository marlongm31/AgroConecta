package com.agroconecta.infrastructure.adapter.out.persistence;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.out.CropRepositoryPort;
import com.agroconecta.domain.model.Crop;
import com.agroconecta.domain.model.CropStatus;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.CropJpaMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.PageMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.repository.CropJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class CropPersistenceAdapter implements CropRepositoryPort {

    private final CropJpaRepository repository;

    public CropPersistenceAdapter(CropJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Crop> findById(Long id) {
        return repository.findById(id).map(CropJpaMapper::toDomain);
    }

    @Override
    public Crop save(Crop crop) {
        return CropJpaMapper.toDomain(repository.save(CropJpaMapper.toEntity(crop)));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public PageResult<Crop> findByOwnerId(Long ownerId, Long parcelId, PageQuery pageQuery) {
        PageRequest pageable = PageRequest.of(pageQuery.page(), pageQuery.size(),
                Sort.by("plantingDate").descending());
        Page<com.agroconecta.infrastructure.adapter.out.persistence.entity.CropJpaEntity> page =
                parcelId == null
                        ? repository.findByOwnerId(ownerId, pageable)
                        : repository.findByParcelId(parcelId, pageable);
        return PageMapper.toPageResult(page, CropJpaMapper::toDomain);
    }

    @Override
    public long countActiveByOwnerId(Long ownerId) {
        List<CropStatus> activeStatuses = Arrays.stream(CropStatus.values())
                .filter(CropStatus::isActive)
                .toList();
        return repository.countByOwnerIdAndStatusIn(ownerId, activeStatuses);
    }

    @Override
    public boolean belongsToOwner(Long cropId, Long ownerId) {
        return repository.existsByIdAndOwnerId(cropId, ownerId);
    }
}
