package com.agroconecta.application.model;

import com.agroconecta.domain.model.AgriculturalActivity;

import java.math.BigDecimal;
import java.util.List;

public record ProducerDashboard(
        long parcelCount,
        long activeCropCount,
        BigDecimal totalExpenses,
        long productionRecordCount,
        long publishedProductCount,
        List<AgriculturalActivity> recentActivities
) {

    public ProducerDashboard {
        recentActivities = List.copyOf(recentActivities);
    }
}
