package com.agroconecta.application.port.out;

import com.agroconecta.domain.model.ProductImage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepositoryPort {

    List<ProductImage> findByProductId(Long productId);

    List<ProductImage> findByProductIds(Collection<Long> productIds);

    Optional<ProductImage> findById(Long imageId);

    long countByProductId(Long productId);

    ProductImage save(ProductImage image);

    void deleteById(Long imageId);
}
