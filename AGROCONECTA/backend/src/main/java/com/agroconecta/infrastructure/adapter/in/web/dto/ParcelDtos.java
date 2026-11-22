package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.application.command.ParcelCommand;
import com.agroconecta.domain.model.Parcel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public final class ParcelDtos {

    private ParcelDtos() {
    }

    public record ParcelRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
            String name,
            @NotBlank(message = "La ubicación es obligatoria")
            @Size(max = 180, message = "La ubicación no puede superar 180 caracteres")
            String location,
            @NotNull(message = "El área es obligatoria")
            @DecimalMin(value = "0.01", message = "El área debe ser mayor que cero")
            @Digits(integer = 10, fraction = 2, message = "El área admite máximo 2 decimales")
            BigDecimal area,
            @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
            String description
    ) {

        public ParcelCommand toCommand() {
            return new ParcelCommand(name, location, area, description);
        }
    }

    public record ParcelResponse(
            Long id,
            Long ownerId,
            String name,
            String location,
            BigDecimal area,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {

        public static ParcelResponse from(Parcel parcel) {
            return new ParcelResponse(parcel.id(), parcel.ownerId(), parcel.name(), parcel.location(),
                    parcel.area(), parcel.description(), parcel.createdAt(), parcel.updatedAt());
        }
    }
}
