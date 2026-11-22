package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.application.command.ProductCommand;
import com.agroconecta.application.model.ProductCatalogItem;
import com.agroconecta.domain.model.MeasurementUnit;
import com.agroconecta.domain.model.Product;
import com.agroconecta.domain.model.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ProductDtos {

    private ProductDtos() {
    }

    public record ProductRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
            String name,
            @NotBlank(message = "La descripción es obligatoria")
            @Size(max = 600, message = "La descripción no puede superar 600 caracteres")
            String description,
            @Size(max = 400, message = "Las características no pueden superar 400 caracteres")
            String characteristics,
            @NotNull(message = "La categoría es obligatoria")
            ProductCategory category,
            @NotNull(message = "El precio es obligatorio")
            @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
            @Digits(integer = 12, fraction = 2, message = "El precio admite máximo 2 decimales")
            BigDecimal price,
            @NotNull(message = "La cantidad disponible es obligatoria")
            @DecimalMin(value = "0.00", message = "La cantidad no puede ser negativa")
            @Digits(integer = 12, fraction = 2, message = "La cantidad admite máximo 2 decimales")
            BigDecimal availableQuantity,
            @NotNull(message = "La unidad de medida es obligatoria")
            MeasurementUnit unit,
            @NotBlank(message = "El teléfono de contacto es obligatorio")
            @Size(max = 40, message = "El teléfono no puede superar 40 caracteres")
            String contactPhone,
            @NotBlank(message = "El correo de contacto es obligatorio")
            @Email(message = "El correo de contacto no es válido")
            @Size(max = 160, message = "El correo no puede superar 160 caracteres")
            String contactEmail,
            boolean active
    ) {

        public ProductCommand toCommand() {
            return new ProductCommand(name, description, characteristics, category, price, availableQuantity, unit,
                    contactPhone, contactEmail, active);
        }
    }

    public record ChangeProductStatusRequest(
            @NotNull(message = "El estado es obligatorio")
            Boolean active
    ) {
    }

    public record ProductResponse(
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
            List<String> imageUrls,
            String producerName,
            String producerLocation,
            Instant createdAt,
            Instant updatedAt
    ) {

        public static ProductResponse from(Product product, List<String> imageUrls) {
            return new ProductResponse(product.id(), product.ownerId(), product.name(), product.description(),
                    product.characteristics(), product.category(), product.price(), product.availableQuantity(),
                    product.unit(), product.contactPhone(), product.contactEmail(), product.active(),
                    product.soldOut(), imageUrls == null ? List.of() : imageUrls, null, null,
                    product.createdAt(), product.updatedAt());
        }

        public static ProductResponse from(ProductCatalogItem item, List<String> imageUrls) {
            Product product = item.product();
            return new ProductResponse(product.id(), product.ownerId(), product.name(), product.description(),
                    product.characteristics(), product.category(), product.price(), product.availableQuantity(),
                    product.unit(), product.contactPhone(), product.contactEmail(), product.active(),
                    product.soldOut(), imageUrls == null ? List.of() : imageUrls, item.producerName(),
                    item.producerLocation(), product.createdAt(), product.updatedAt());
        }
    }
}
