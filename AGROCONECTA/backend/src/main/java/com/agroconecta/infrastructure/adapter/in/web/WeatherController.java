package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.port.in.WeatherUseCase;
import com.agroconecta.domain.model.Weather;
import com.agroconecta.domain.service.WeatherAdvisor;
import com.agroconecta.infrastructure.adapter.in.security.AuthenticatedUser;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.ApiResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.WeatherDtos.WeatherReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@Tag(name = "Clima", description = "Pronóstico y recomendaciones agrícolas por ubicación o parcela")
public class WeatherController {

    private final WeatherUseCase weatherUseCase;
    private final WeatherAdvisor weatherAdvisor;

    public WeatherController(WeatherUseCase weatherUseCase, WeatherAdvisor weatherAdvisor) {
        this.weatherUseCase = weatherUseCase;
        this.weatherAdvisor = weatherAdvisor;
    }

    @GetMapping
    @Operation(summary = "Consultar el pronóstico para un municipio o ciudad")
    public ResponseEntity<ApiResponse<WeatherReportResponse>> report(@RequestParam String location) {
        Weather.WeatherReport report = weatherUseCase.report(location);
        return ResponseEntity.ok(ApiResponse.ok(WeatherReportResponse.from(report, weatherAdvisor)));
    }

    @GetMapping("/parcels/{parcelId}")
    @Operation(summary = "Consultar el pronóstico de una parcela y recomendaciones según sus cultivos")
    public ResponseEntity<ApiResponse<WeatherReportResponse>> reportForParcel(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long parcelId) {
        Weather.WeatherReport report = weatherUseCase.reportForParcel(user.id(), parcelId);
        return ResponseEntity.ok(ApiResponse.ok(WeatherReportResponse.from(report, weatherAdvisor)));
    }
}
