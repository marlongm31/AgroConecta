package com.agroconecta.application.model;

import com.agroconecta.domain.model.Product;

public record ProductCatalogItem(Product product, String producerName, String producerLocation) {
}
