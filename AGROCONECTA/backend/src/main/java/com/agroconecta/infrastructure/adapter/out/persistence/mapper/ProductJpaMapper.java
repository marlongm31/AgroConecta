package com.agroconecta.infrastructure.adapter.out.persistence.mapper;

import com.agroconecta.domain.model.Product;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.ProductJpaEntity;

public final class ProductJpaMapper {

    private ProductJpaMapper() {
    }

    public static Product toDomain(ProductJpaEntity entity) {
        return new Product(
                entity.getId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCharacteristics(),
                entity.getCategory(),
                entity.getPrice(),
                entity.getAvailableQuantity(),
                entity.getUnit(),
                entity.getContactPhone(),
                entity.getContactEmail(),
                entity.isActive(),
                Product.deriveSoldOut(entity.getAvailableQuantity()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ProductJpaEntity toEntity(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(product.id());
        entity.setOwnerId(product.ownerId());
        entity.setName(product.name());
        entity.setDescription(product.description());
        entity.setCharacteristics(product.characteristics());
        entity.setCategory(product.category());
        entity.setPrice(product.price());
        entity.setAvailableQuantity(product.availableQuantity());
        entity.setUnit(product.unit());
        entity.setContactPhone(product.contactPhone());
        entity.setContactEmail(product.contactEmail());
        entity.setActive(product.active());
        entity.setSoldOut(Product.deriveSoldOut(product.availableQuantity()));
        entity.setCreatedAt(product.createdAt());
        entity.setUpdatedAt(product.updatedAt());
        return entity;
    }
}
