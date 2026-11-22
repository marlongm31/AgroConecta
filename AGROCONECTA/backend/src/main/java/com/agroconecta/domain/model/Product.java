package com.agroconecta.domain.model;

import com.agroconecta.domain.validation.DomainValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record Product(
        Long id,
        Long ownerId,
        String name,
        String description,
        String characteristics,
        ProductCategory category,
        BigDecimal price,
        BigDecimal availableQuantity,
        MeasurementUnit unit,
        String contactPhone,
        String contactEmail,
        boolean active,
        boolean soldOut,
        Instant createdAt,
        Instant updatedAt
) {

    public Product {
        Objects.requireNonNull(ownerId, "El productor es obligatorio");
        name = DomainValidator.requiredText(name, "nombre del producto");
        description = DomainValidator.requiredText(description, "descripción");
        characteristics = DomainValidator.optionalText(characteristics, 400, "características");
        Objects.requireNonNull(category, "La categoría es obligatoria");
        price = DomainValidator.positive(price, "precio");
        availableQuantity = DomainValidator.nonNegative(availableQuantity, "cantidad disponible");
        Objects.requireNonNull(unit, "La unidad de medida es obligatoria");
        contactPhone = DomainValidator.requiredText(contactPhone, "teléfono de contacto");
        contactEmail = DomainValidator.email(contactEmail);
        soldOut = deriveSoldOut(availableQuantity);
    }

    public static boolean deriveSoldOut(BigDecimal availableQuantity) {
        return availableQuantity.compareTo(BigDecimal.ZERO) <= 0;
    }

    public static Product create(Long ownerId, String name, String description, ProductCategory category,
                                 BigDecimal price, BigDecimal availableQuantity, MeasurementUnit unit,
                                 String contactPhone, String contactEmail) {
        return create(ownerId, name, description, null, category, price, availableQuantity, unit,
                contactPhone, contactEmail);
    }

    public static Product create(Long ownerId, String name, String description, String characteristics,
                                 ProductCategory category, BigDecimal price, BigDecimal availableQuantity,
                                 MeasurementUnit unit, String contactPhone, String contactEmail) {
        return new Product(null, ownerId, name, description, characteristics, category, price, availableQuantity,
                unit, contactPhone, contactEmail, true, deriveSoldOut(availableQuantity), null, null);
    }

    public Product update(String name, String description, String characteristics, ProductCategory category,
                          BigDecimal price, BigDecimal availableQuantity, MeasurementUnit unit,
                          String contactPhone, String contactEmail, boolean active) {
        return new Product(id, ownerId, name, description, characteristics, category, price, availableQuantity,
                unit, contactPhone, contactEmail, active, deriveSoldOut(availableQuantity), createdAt, updatedAt);
    }

    public Product withStatus(boolean newActive) {
        return new Product(id, ownerId, name, description, characteristics, category, price, availableQuantity,
                unit, contactPhone, contactEmail, newActive, soldOut, createdAt, updatedAt);
    }

    public boolean belongsTo(Long userId) {
        return ownerId.equals(userId);
    }
}
