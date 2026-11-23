package com.agroconecta.infrastructure.adapter.out.persistence.mapper;

import com.agroconecta.domain.model.ProductionRecord;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.ProductionJpaEntity;

public final class ProductionJpaMapper {

    private ProductionJpaMapper() {
    }

    public static ProductionRecord toDomain(ProductionJpaEntity entity) {
        return new ProductionRecord(
                entity.getId(),
                entity.getCropId(),
                entity.getQuantity(),
                entity.getUnit(),
                entity.getHarvestDate(),
                entity.getObservations(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ProductionJpaEntity toEntity(ProductionRecord record) {
        ProductionJpaEntity entity = new ProductionJpaEntity();
        entity.setId(record.id());
        entity.setCropId(record.cropId());
        entity.setQuantity(record.quantity());
        entity.setUnit(record.unit());
        entity.setHarvestDate(record.harvestDate());
        entity.setObservations(record.observations());
        entity.setCreatedAt(record.createdAt());
        entity.setUpdatedAt(record.updatedAt());
        return entity;
    }
}
