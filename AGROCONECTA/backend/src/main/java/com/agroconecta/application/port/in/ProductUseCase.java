package com.agroconecta.application.port.in;

import com.agroconecta.application.command.ProductCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.model.ProductCatalogItem;
import com.agroconecta.domain.model.Product;
import com.agroconecta.domain.model.ProductCategory;

public interface ProductUseCase {

    PageResult<ProductCatalogItem> searchCatalog(String search, ProductCategory category, PageQuery pageQuery);

    ProductCatalogItem getCatalogItem(Long productId);

    PageResult<Product> listOwn(Long ownerId, PageQuery pageQuery);

    Product getOwn(Long ownerId, Long productId);

    Product create(Long ownerId, ProductCommand command);

    Product update(Long ownerId, Long productId, ProductCommand command);

    Product changeStatus(Long ownerId, Long productId, boolean active);

    void delete(Long ownerId, Long productId);
}
