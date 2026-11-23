package com.agroconecta.infrastructure.adapter.out.persistence.mapper;

import com.agroconecta.domain.model.AgriculturalActivity;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.ActivityJpaEntity;

public final class ActivityJpaMapper {

    private ActivityJpaMapper() {
    }

    public static AgriculturalActivity toDomain(ActivityJpaEntity entity) {
        return new AgriculturalActivity(
                entity.getId(),
                entity.getCropId(),
                entity.getType(),
                entity.getActivityDate(),
                entity.getDescription(),
                entity.getObservations(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ActivityJpaEntity toEntity(AgriculturalActivity activity) {
        ActivityJpaEntity entity = new ActivityJpaEntity();
        entity.setId(activity.id());
        entity.setCropId(activity.cropId());
        entity.setType(activity.type());
        entity.setActivityDate(activity.activityDate());
        entity.setDescription(activity.description());
        entity.setObservations(activity.observations());
        entity.setCreatedAt(activity.createdAt());
        entity.setUpdatedAt(activity.updatedAt());
        return entity;
    }
}
