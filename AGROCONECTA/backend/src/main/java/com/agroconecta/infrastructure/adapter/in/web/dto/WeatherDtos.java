package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.domain.model.Weather;
import com.agroconecta.domain.service.WeatherAdvisor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class WeatherDtos {

    private WeatherDtos() {
    }

    public record WeatherDayResponse(
            LocalDate date,
            int weatherCode,
            String condition,
            double temperatureMax,
            double temperatureMin,
            int precipitationProbability,
            double precipitationMm,
            double windMaxKmh
    ) {
    }

    public record WeatherReportResponse(
            String name,
            String region,
            String country,
            double latitude,
            double longitude,
            String timezone,
            List<WeatherDayResponse> days,
            List<String> advisories,
            Instant fetchedAt
    ) {

        public static WeatherReportResponse from(Weather.WeatherReport report, WeatherAdvisor advisor) {
            List<WeatherDayResponse> days = report.days().stream()
                    .map(day -> new WeatherDayResponse(day.date(), day.weatherCode(),
                            advisor.conditionLabel(day.weatherCode()), day.temperatureMax(), day.temperatureMin(),
                            day.precipitationProbability(), day.precipitationMm(), day.windMaxKmh()))
                    .toList();
            Weather.GeoLocation location = report.location();
            return new WeatherReportResponse(location.name(), location.region(), location.country(),
                    location.latitude(), location.longitude(), location.timezone(), days, report.advisories(),
                    report.fetchedAt());
        }
    }
}
