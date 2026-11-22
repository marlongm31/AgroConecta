package com.agroconecta.domain.model;

import java.time.Instant;

public record ProductImage(
        Long id,
        Long productId,
        String imageUrl,
        String contentType,
        long sizeBytes,
        int position,
        Instant createdAt
) {

    public static ProductImage create(Long productId, String imageUrl, String contentType, long sizeBytes,
                                      int position) {
        return new ProductImage(null, productId, imageUrl, contentType, sizeBytes, position, null);
    }
}
