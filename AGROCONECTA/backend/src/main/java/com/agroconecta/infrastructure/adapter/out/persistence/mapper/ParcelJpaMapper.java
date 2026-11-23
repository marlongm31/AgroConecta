package com.agroconecta.infrastructure.adapter.out.persistence.mapper;

import com.agroconecta.domain.model.Parcel;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.ParcelJpaEntity;

public final class ParcelJpaMapper {

    private ParcelJpaMapper() {
    }

    public static Parcel toDomain(ParcelJpaEntity entity) {
        return new Parcel(
                entity.getId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getLocation(),
                entity.getArea(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ParcelJpaEntity toEntity(Parcel parcel) {
        ParcelJpaEntity entity = new ParcelJpaEntity();
        entity.setId(parcel.id());
        entity.setOwnerId(parcel.ownerId());
        entity.setName(parcel.name());
        entity.setLocation(parcel.location());
        entity.setArea(parcel.area());
        entity.setDescription(parcel.description());
        entity.setCreatedAt(parcel.createdAt());
        entity.setUpdatedAt(parcel.updatedAt());
        return entity;
    }
}
