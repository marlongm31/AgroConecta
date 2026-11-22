package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.application.command.ProductionCommand;
import com.agroconecta.domain.model.MeasurementUnit;
import com.agroconecta.domain.model.ProductionRecord;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class ProductionDtos {

    private ProductionDtos() {
    }

    public record ProductionRequest(
            @NotNull(message = "El cultivo es obligatorio")
            Long cropId,
            @NotNull(message = "La cantidad es obligatoria")
            @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor que cero")
            @Digits(integer = 12, fraction = 2, message = "La cantidad admite máximo 2 decimales")
            BigDecimal quantity,
            @NotNull(message = "La unidad de medida es obligatoria")
            MeasurementUnit unit,
            @NotNull(message = "La fecha de cosecha es obligatoria")
            LocalDate harvestDate,
            @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
            String observations
    ) {

        public ProductionCommand toCommand() {
            return new ProductionCommand(cropId, quantity, unit, harvestDate, observations);
        }
    }

    public record ProductionResponse(
            Long id,
            Long cropId,
            BigDecimal quantity,
            MeasurementUnit unit,
            LocalDate harvestDate,
            String observations,
            Instant createdAt,
            Instant updatedAt
    ) {

        public static ProductionResponse from(ProductionRecord record) {
            return new ProductionResponse(record.id(), record.cropId(), record.quantity(), record.unit(),
                    record.harvestDate(), record.observations(), record.createdAt(), record.updatedAt());
        }
    }
}
