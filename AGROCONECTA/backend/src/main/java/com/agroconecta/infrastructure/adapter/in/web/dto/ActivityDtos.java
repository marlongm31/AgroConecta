package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.application.command.ActivityCommand;
import com.agroconecta.domain.model.ActivityType;
import com.agroconecta.domain.model.AgriculturalActivity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

public final class ActivityDtos {

    private ActivityDtos() {
    }

    public record ActivityRequest(
            @NotNull(message = "El cultivo es obligatorio")
            Long cropId,
            @NotNull(message = "El tipo de actividad es obligatorio")
            ActivityType type,
            @NotNull(message = "La fecha es obligatoria")
            LocalDate activityDate,
            @NotBlank(message = "La descripción es obligatoria")
            @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
            String description,
            @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
            String observations
    ) {

        public ActivityCommand toCommand() {
            return new ActivityCommand(cropId, type, activityDate, description, observations);
        }
    }

    public record ActivityResponse(
            Long id,
            Long cropId,
            ActivityType type,
            LocalDate activityDate,
            String description,
            String observations,
            Instant createdAt,
            Instant updatedAt
    ) {

        public static ActivityResponse from(AgriculturalActivity activity) {
            return new ActivityResponse(activity.id(), activity.cropId(), activity.type(), activity.activityDate(),
                    activity.description(), activity.observations(), activity.createdAt(), activity.updatedAt());
        }
    }
}
