package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.port.in.DashboardUseCase;
import com.agroconecta.application.port.in.ProductImageUseCase;
import com.agroconecta.application.model.BuyerDashboard;
import com.agroconecta.infrastructure.adapter.in.security.AuthenticatedUser;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.ApiResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.DashboardDtos.BuyerDashboardResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.DashboardDtos.ProducerDashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Indicadores calculados con información real de la base de datos")
public class DashboardController {

    private final DashboardUseCase dashboardUseCase;
    private final ProductImageUseCase productImageUseCase;

    public DashboardController(DashboardUseCase dashboardUseCase, ProductImageUseCase productImageUseCase) {
        this.dashboardUseCase = dashboardUseCase;
        this.productImageUseCase = productImageUseCase;
    }

    @GetMapping("/producer")
    @Operation(summary = "Dashboard del productor autenticado")
    public ResponseEntity<ApiResponse<ProducerDashboardResponse>> producer(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.ok(
                ProducerDashboardResponse.from(dashboardUseCase.producerDashboard(user.id()))));
    }

    @GetMapping("/buyer")
    @Operation(summary = "Dashboard del comprador autenticado")
    public ResponseEntity<ApiResponse<BuyerDashboardResponse>> buyer() {
        BuyerDashboard dashboard = dashboardUseCase.buyerDashboard();
        Map<Long, List<String>> images = productImageUseCase.imageUrlsByProduct(
                dashboard.recentProducts().stream().map(item -> item.product().id()).toList());
        return ResponseEntity.ok(ApiResponse.ok(BuyerDashboardResponse.from(dashboard, images)));
    }
}
