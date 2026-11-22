package com.agroconecta.application.command;

import com.agroconecta.domain.model.ActivityType;

import java.time.LocalDate;

public record ActivityCommand(
        Long cropId,
        ActivityType type,
        LocalDate activityDate,
        String description,
        String observations
) {
}
