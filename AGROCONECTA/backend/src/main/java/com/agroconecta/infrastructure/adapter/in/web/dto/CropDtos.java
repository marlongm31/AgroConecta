package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.application.command.CropCommand;
import com.agroconecta.domain.model.Crop;
import com.agroconecta.domain.model.CropStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

public final class CropDtos {

    private CropDtos() {
    }

    public record CropRequest(
            @NotNull(message = "La parcela es obligatoria")
            Long parcelId,
            @NotBlank(message = "El nombre del cultivo es obligatorio")
            @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
            String name,
            @Size(max = 120, message = "La variedad no puede superar 120 caracteres")
            String variety,
            @NotNull(message = "La fecha de siembra es obligatoria")
            LocalDate plantingDate,
            LocalDate estimatedHarvestDate,
            @NotNull(message = "El estado es obligatorio")
            CropStatus status,
            @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
            String observations
    ) {

        public CropCommand toCommand() {
            return new CropCommand(parcelId, name, variety, plantingDate, estimatedHarvestDate, status, observations);
        }
    }

    public record CropResponse(
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

        public static CropResponse from(Crop crop) {
            return new CropResponse(crop.id(), crop.parcelId(), crop.name(), crop.variety(), crop.plantingDate(),
                    crop.estimatedHarvestDate(), crop.status(), crop.observations(), crop.createdAt(),
                    crop.updatedAt());
        }
    }

    public record ChangeCropStatusRequest(
            @NotNull(message = "El estado es obligatorio")
            CropStatus status
    ) {
    }
}
