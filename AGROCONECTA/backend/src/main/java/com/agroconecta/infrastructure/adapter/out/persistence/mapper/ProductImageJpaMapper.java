package com.agroconecta.infrastructure.adapter.out.persistence.mapper;

import com.agroconecta.domain.model.ProductImage;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.ProductImageJpaEntity;

public final class ProductImageJpaMapper {

    private ProductImageJpaMapper() {
    }

    public static ProductImage toDomain(ProductImageJpaEntity entity) {
        return new ProductImage(entity.getId(), entity.getProductId(), entity.getImageUrl(),
                entity.getContentType(), entity.getSizeBytes(), entity.getPosition(), entity.getCreatedAt());
    }

    public static ProductImageJpaEntity toEntity(ProductImage image) {
        ProductImageJpaEntity entity = new ProductImageJpaEntity();
        entity.setId(image.id());
        entity.setProductId(image.productId());
        entity.setImageUrl(image.imageUrl());
        entity.setContentType(image.contentType());
        entity.setSizeBytes(image.sizeBytes());
        entity.setPosition(image.position());
        entity.setCreatedAt(image.createdAt());
        return entity;
    }
}
