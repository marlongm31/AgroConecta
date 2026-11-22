package com.agroconecta.application.command;

import com.agroconecta.domain.model.MeasurementUnit;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductionCommand(
        Long cropId,
        BigDecimal quantity,
        MeasurementUnit unit,
        LocalDate harvestDate,
        String observations
) {
}
