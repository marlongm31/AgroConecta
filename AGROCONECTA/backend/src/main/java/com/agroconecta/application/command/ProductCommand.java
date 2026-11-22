package com.agroconecta.application.command;

import com.agroconecta.domain.model.MeasurementUnit;
import com.agroconecta.domain.model.ProductCategory;

import java.math.BigDecimal;

public record ProductCommand(
        String name,
        String description,
        String characteristics,
        ProductCategory category,
        BigDecimal price,
        BigDecimal availableQuantity,
        MeasurementUnit unit,
        String contactPhone,
        String contactEmail,
        boolean active
) {
}
