package com.agroconecta.application.port.out;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.model.ProductCatalogItem;
import com.agroconecta.domain.model.Product;
import com.agroconecta.domain.model.ProductCategory;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {

    Optional<Product> findById(Long id);

    Product save(Product product);

    void deleteById(Long id);

    PageResult<Product> findByOwnerId(Long ownerId, PageQuery pageQuery);

    PageResult<ProductCatalogItem> searchActive(String search, ProductCategory category, PageQuery pageQuery);

    Optional<ProductCatalogItem> findActiveById(Long id);

    long countActiveByOwnerId(Long ownerId);

    long countActive();

    List<ProductCatalogItem> findRecentActive(int limit);
}
