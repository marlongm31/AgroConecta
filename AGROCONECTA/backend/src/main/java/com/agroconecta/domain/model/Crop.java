package com.agroconecta.domain.model;

import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.validation.DomainValidator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public record Crop(
        Long id,
        Long parcelId,
        String name,
        String variety,
        LocalDate plantingDate,
        LocalDate estimatedHarvestDate,
        CropStatus status,
        String observations,
        Instant createdAt,
        Instant updatedAt
) {

    public Crop {
        Objects.requireNonNull(parcelId, "La parcela es obligatoria");
        name = DomainValidator.requiredText(name, "nombre del cultivo");
        variety = DomainValidator.optionalText(variety);
        Objects.requireNonNull(plantingDate, "La fecha de siembra es obligatoria");
        if (estimatedHarvestDate != null && estimatedHarvestDate.isBefore(plantingDate)) {
            throw new BusinessRuleException("La fecha estimada de cosecha no puede ser anterior a la siembra");
        }
        Objects.requireNonNull(status, "El estado del cultivo es obligatorio");
        observations = DomainValidator.optionalText(observations);
    }

    public static Crop create(Long parcelId, String name, String variety, LocalDate plantingDate,
                              LocalDate estimatedHarvestDate, CropStatus status, String observations) {
        return new Crop(null, parcelId, name, variety, plantingDate, estimatedHarvestDate, status, observations, null, null);
    }

    public Crop update(Long parcelId, String name, String variety, LocalDate plantingDate,
                       LocalDate estimatedHarvestDate, CropStatus status, String observations) {
        return new Crop(id, parcelId, name, variety, plantingDate, estimatedHarvestDate, status, observations,
                createdAt, updatedAt);
    }

    public Crop changeStatus(CropStatus newStatus) {
        return new Crop(id, parcelId, name, variety, plantingDate, estimatedHarvestDate, newStatus,
                observations, createdAt, updatedAt);
    }
}
