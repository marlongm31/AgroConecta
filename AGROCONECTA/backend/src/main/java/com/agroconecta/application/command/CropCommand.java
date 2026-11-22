package com.agroconecta.application.command;

import com.agroconecta.domain.model.CropStatus;

import java.time.LocalDate;

public record CropCommand(
        Long parcelId,
        String name,
        String variety,
        LocalDate plantingDate,
        LocalDate estimatedHarvestDate,
        CropStatus status,
        String observations
) {
}
