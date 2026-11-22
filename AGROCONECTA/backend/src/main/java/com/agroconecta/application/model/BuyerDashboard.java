package com.agroconecta.application.model;

import java.util.List;

public record BuyerDashboard(
        long availableProductCount,
        List<ProductCatalogItem> recentProducts
) {

    public BuyerDashboard {
        recentProducts = List.copyOf(recentProducts);
    }
}
