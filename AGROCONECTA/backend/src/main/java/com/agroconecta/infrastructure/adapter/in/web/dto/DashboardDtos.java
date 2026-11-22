package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.application.model.BuyerDashboard;
import com.agroconecta.application.model.ProducerDashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record ProducerDashboardResponse(
            long parcelCount,
            long activeCropCount,
            BigDecimal totalExpenses,
            long productionRecordCount,
            long publishedProductCount,
            List<ActivityDtos.ActivityResponse> recentActivities
    ) {

        public static ProducerDashboardResponse from(ProducerDashboard dashboard) {
            List<ActivityDtos.ActivityResponse> activities = dashboard.recentActivities().stream()
                    .map(ActivityDtos.ActivityResponse::from)
                    .toList();
            return new ProducerDashboardResponse(dashboard.parcelCount(), dashboard.activeCropCount(),
                    dashboard.totalExpenses(), dashboard.productionRecordCount(), dashboard.publishedProductCount(),
                    activities);
        }
    }

    public record BuyerDashboardResponse(
            long availableProductCount,
            List<ProductDtos.ProductResponse> recentProducts
    ) {

        public static BuyerDashboardResponse from(BuyerDashboard dashboard, Map<Long, List<String>> imageUrls) {
            List<ProductDtos.ProductResponse> products = dashboard.recentProducts().stream()
                    .map(item -> ProductDtos.ProductResponse.from(item, imageUrls.get(item.product().id())))
                    .toList();
            return new BuyerDashboardResponse(dashboard.availableProductCount(), products);
        }
    }
}
