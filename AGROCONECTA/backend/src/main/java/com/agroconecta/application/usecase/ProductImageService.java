package com.agroconecta.application.usecase;

import com.agroconecta.application.command.ImageUploadCommand;
import com.agroconecta.application.model.StoredFile;
import com.agroconecta.application.port.in.ProductImageUseCase;
import com.agroconecta.application.port.out.FileStoragePort;
import com.agroconecta.application.port.out.ProductImageRepositoryPort;
import com.agroconecta.application.port.out.ProductRepositoryPort;
import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.model.Product;
import com.agroconecta.domain.model.ProductImage;
import com.agroconecta.domain.validation.ImageValidator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductImageService implements ProductImageUseCase {

    private static final int MAX_IMAGES = 6;

    private final ProductRepositoryPort productRepository;
    private final ProductImageRepositoryPort imageRepository;
    private final FileStoragePort fileStorage;

    public ProductImageService(ProductRepositoryPort productRepository,
                               ProductImageRepositoryPort imageRepository,
                               FileStoragePort fileStorage) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.fileStorage = fileStorage;
    }

    @Override
    public List<String> imageUrlsFor(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return imageRepository.findByProductIds(productIds).stream().map(ProductImage::imageUrl).toList();
    }

    @Override
    public List<ProductImage> listForProduct(Long ownerId, Long productId) {
        requireOwnedProduct(ownerId, productId);
        return imageRepository.findByProductId(productId);
    }

    @Override
    public ProductImage upload(Long ownerId, Long productId, ImageUploadCommand command) {
        requireOwnedProduct(ownerId, productId);
        if (imageRepository.countByProductId(productId) >= MAX_IMAGES) {
            throw new BusinessRuleException("Un producto puede tener como máximo " + MAX_IMAGES + " fotografías");
        }
        byte[] content = command.content();
        String extension = ImageValidator.detectExtension(content);
        StoredFile stored = fileStorage.storeImage("products", content, extension);
        int position = imageRepository.findByProductId(productId).size();
        return imageRepository.save(ProductImage.create(productId, stored.url(), stored.contentType(),
                stored.sizeBytes(), position));
    }

    @Override
    public void delete(Long ownerId, Long imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Imagen", imageId));
        requireOwnedProduct(ownerId, image.productId());
        imageRepository.deleteById(imageId);
        fileStorage.delete(image.imageUrl());
    }

    @Override
    public Map<Long, List<String>> imageUrlsByProduct(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return imageRepository.findByProductIds(productIds).stream()
                .collect(Collectors.groupingBy(ProductImage::productId,
                        Collectors.mapping(ProductImage::imageUrl, Collectors.toList())));
    }

    private Product requireOwnedProduct(Long ownerId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Producto", productId));
        if (!product.belongsTo(ownerId)) {
            throw new ForbiddenException("No tienes permiso para modificar este producto");
        }
        return product;
    }
}
