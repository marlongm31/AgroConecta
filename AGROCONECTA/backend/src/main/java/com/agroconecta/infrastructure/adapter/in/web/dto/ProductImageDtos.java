package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.domain.model.ProductImage;

import java.time.Instant;

public final class ProductImageDtos {

    private ProductImageDtos() {
    }

    public record ProductImageResponse(
            Long id,
            Long productId,
            String imageUrl,
            String contentType,
            long sizeBytes,
            int position,
            Instant createdAt
    ) {

        public static ProductImageResponse from(ProductImage image) {
            return new ProductImageResponse(image.id(), image.productId(), image.imageUrl(), image.contentType(),
                    image.sizeBytes(), image.position(), image.createdAt());
        }
    }
}
