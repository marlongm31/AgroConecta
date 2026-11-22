package com.agroconecta.domain.model;

import com.agroconecta.domain.validation.DomainValidator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public record AgriculturalActivity(
        Long id,
        Long cropId,
        ActivityType type,
        LocalDate activityDate,
        String description,
        String observations,
        Instant createdAt,
        Instant updatedAt
) {

    public AgriculturalActivity {
        Objects.requireNonNull(cropId, "El cultivo es obligatorio");
        Objects.requireNonNull(type, "El tipo de actividad es obligatorio");
        Objects.requireNonNull(activityDate, "La fecha de la actividad es obligatoria");
        description = DomainValidator.requiredText(description, "descripción");
        observations = DomainValidator.optionalText(observations);
    }

    public static AgriculturalActivity create(Long cropId, ActivityType type, LocalDate activityDate,
                                              String description, String observations) {
        return new AgriculturalActivity(null, cropId, type, activityDate, description, observations, null, null);
    }

    public AgriculturalActivity update(Long cropId, ActivityType type, LocalDate activityDate,
                                       String description, String observations) {
        return new AgriculturalActivity(id, cropId, type, activityDate, description, observations, createdAt, updatedAt);
    }
}
