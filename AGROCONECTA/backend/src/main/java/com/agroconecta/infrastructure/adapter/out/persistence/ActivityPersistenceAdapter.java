package com.agroconecta.infrastructure.adapter.out.persistence;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.out.ActivityRepositoryPort;
import com.agroconecta.domain.model.AgriculturalActivity;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.ActivityJpaEntity;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.ActivityJpaMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.PageMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.repository.ActivityJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ActivityPersistenceAdapter implements ActivityRepositoryPort {

    private final ActivityJpaRepository repository;

    public ActivityPersistenceAdapter(ActivityJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AgriculturalActivity> findById(Long id) {
        return repository.findById(id).map(ActivityJpaMapper::toDomain);
    }

    @Override
    public AgriculturalActivity save(AgriculturalActivity activity) {
        return ActivityJpaMapper.toDomain(repository.save(ActivityJpaMapper.toEntity(activity)));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public PageResult<AgriculturalActivity> findByOwnerId(Long ownerId, Long cropId, PageQuery pageQuery) {
        PageRequest pageable = PageRequest.of(pageQuery.page(), pageQuery.size(),
                Sort.by("activityDate").descending());
        Page<ActivityJpaEntity> page = cropId == null
                ? repository.findByOwnerId(ownerId, pageable)
                : repository.findByCropId(cropId, pageable);
        return PageMapper.toPageResult(page, ActivityJpaMapper::toDomain);
    }

    @Override
    public List<AgriculturalActivity> findRecentByOwnerId(Long ownerId, int limit) {
        PageRequest pageable = PageRequest.of(0, limit,
                Sort.by(Sort.Order.desc("activityDate"), Sort.Order.desc("id")));
        return repository.findByOwnerId(ownerId, pageable).getContent().stream()
                .map(ActivityJpaMapper::toDomain)
                .toList();
    }

    @Override
    public long countByOwnerId(Long ownerId) {
        return repository.countByOwnerId(ownerId);
    }

    @Override
    public boolean belongsToOwner(Long activityId, Long ownerId) {
        return repository.existsByIdAndOwnerId(activityId, ownerId);
    }
}
