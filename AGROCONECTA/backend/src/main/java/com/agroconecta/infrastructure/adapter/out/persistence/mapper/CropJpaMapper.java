package com.agroconecta.infrastructure.adapter.out.persistence.mapper;

import com.agroconecta.domain.model.Crop;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.CropJpaEntity;

public final class CropJpaMapper {

    private CropJpaMapper() {
    }

    public static Crop toDomain(CropJpaEntity entity) {
        return new Crop(
                entity.getId(),
                entity.getParcelId(),
                entity.getName(),
                entity.getVariety(),
                entity.getPlantingDate(),
                entity.getEstimatedHarvestDate(),
                entity.getStatus(),
                entity.getObservations(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static CropJpaEntity toEntity(Crop crop) {
        CropJpaEntity entity = new CropJpaEntity();
        entity.setId(crop.id());
        entity.setParcelId(crop.parcelId());
        entity.setName(crop.name());
        entity.setVariety(crop.variety());
        entity.setPlantingDate(crop.plantingDate());
        entity.setEstimatedHarvestDate(crop.estimatedHarvestDate());
        entity.setStatus(crop.status());
        entity.setObservations(crop.observations());
        entity.setCreatedAt(crop.createdAt());
        entity.setUpdatedAt(crop.updatedAt());
        return entity;
    }
}
