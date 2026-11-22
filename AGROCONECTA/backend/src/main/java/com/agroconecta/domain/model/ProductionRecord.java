package com.agroconecta.domain.model;

import com.agroconecta.domain.validation.DomainValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public record ProductionRecord(
        Long id,
        Long cropId,
        BigDecimal quantity,
        MeasurementUnit unit,
        LocalDate harvestDate,
        String observations,
        Instant createdAt,
        Instant updatedAt
) {

    public ProductionRecord {
        Objects.requireNonNull(cropId, "El cultivo es obligatorio");
        quantity = DomainValidator.positive(quantity, "cantidad");
        Objects.requireNonNull(unit, "La unidad de medida es obligatoria");
        Objects.requireNonNull(harvestDate, "La fecha de cosecha es obligatoria");
        observations = DomainValidator.optionalText(observations);
    }

    public static ProductionRecord create(Long cropId, BigDecimal quantity, MeasurementUnit unit,
                                          LocalDate harvestDate, String observations) {
        return new ProductionRecord(null, cropId, quantity, unit, harvestDate, observations, null, null);
    }

    public ProductionRecord update(Long cropId, BigDecimal quantity, MeasurementUnit unit,
                                   LocalDate harvestDate, String observations) {
        return new ProductionRecord(id, cropId, quantity, unit, harvestDate, observations, createdAt, updatedAt);
    }
}
