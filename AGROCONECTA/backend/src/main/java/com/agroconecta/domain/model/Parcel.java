package com.agroconecta.domain.model;

import com.agroconecta.domain.validation.DomainValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record Parcel(
        Long id,
        Long ownerId,
        String name,
        String location,
        BigDecimal area,
        String description,
        Instant createdAt,
        Instant updatedAt
) {

    public Parcel {
        Objects.requireNonNull(ownerId, "El propietario es obligatorio");
        name = DomainValidator.requiredText(name, "nombre");
        location = DomainValidator.requiredText(location, "ubicación");
        area = DomainValidator.positive(area, "área");
        description = DomainValidator.optionalText(description);
    }

    public static Parcel create(Long ownerId, String name, String location, BigDecimal area, String description) {
        return new Parcel(null, ownerId, name, location, area, description, null, null);
    }

    public Parcel update(String name, String location, BigDecimal area, String description) {
        return new Parcel(id, ownerId, name, location, area, description, createdAt, updatedAt);
    }

    public boolean belongsTo(Long userId) {
        return ownerId.equals(userId);
    }
}
