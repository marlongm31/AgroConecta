package com.agroconecta.application.port.in;

import com.agroconecta.application.command.ImageUploadCommand;
import com.agroconecta.domain.model.ProductImage;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ProductImageUseCase {

    List<String> imageUrlsFor(Collection<Long> productIds);

    Map<Long, List<String>> imageUrlsByProduct(Collection<Long> productIds);

    List<ProductImage> listForProduct(Long ownerId, Long productId);

    ProductImage upload(Long ownerId, Long productId, ImageUploadCommand command);

    void delete(Long ownerId, Long imageId);
}
