package com.agroconecta.application.usecase;

import com.agroconecta.application.command.ProductCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.model.ProductCatalogItem;
import com.agroconecta.application.port.in.ProductUseCase;
import com.agroconecta.application.port.out.ProductRepositoryPort;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.model.Product;
import com.agroconecta.domain.model.ProductCategory;

public class ProductService implements ProductUseCase {

    private final ProductRepositoryPort productRepository;

    public ProductService(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public PageResult<ProductCatalogItem> searchCatalog(String search, ProductCategory category, PageQuery pageQuery) {
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        return productRepository.searchActive(normalizedSearch, category, pageQuery);
    }

    @Override
    public ProductCatalogItem getCatalogItem(Long productId) {
        return productRepository.findActiveById(productId)
                .orElseThrow(() -> new NotFoundException("Producto", productId));
    }

    @Override
    public PageResult<Product> listOwn(Long ownerId, PageQuery pageQuery) {
        return productRepository.findByOwnerId(ownerId, pageQuery);
    }

    @Override
    public Product getOwn(Long ownerId, Long productId) {
        return requireOwnedProduct(ownerId, productId);
    }

    @Override
    public Product create(Long ownerId, ProductCommand command) {
        Product product = Product.create(ownerId, command.name(), command.description(), command.characteristics(),
                command.category(), command.price(), command.availableQuantity(), command.unit(),
                command.contactPhone(), command.contactEmail());
        return productRepository.save(product);
    }

    @Override
    public Product update(Long ownerId, Long productId, ProductCommand command) {
        Product product = requireOwnedProduct(ownerId, productId);
        Product updated = product.update(command.name(), command.description(), command.characteristics(),
                command.category(), command.price(), command.availableQuantity(), command.unit(),
                command.contactPhone(), command.contactEmail(), command.active());
        return productRepository.save(updated);
    }

    @Override
    public Product changeStatus(Long ownerId, Long productId, boolean active) {
        Product product = requireOwnedProduct(ownerId, productId);
        return productRepository.save(product.withStatus(active));
    }

    @Override
    public void delete(Long ownerId, Long productId) {
        requireOwnedProduct(ownerId, productId);
        productRepository.deleteById(productId);
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
