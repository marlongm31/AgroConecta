package com.agroconecta.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class Weather {

    private Weather() {
    }

    public record GeoLocation(
            String name,
            String region,
            String country,
            double latitude,
            double longitude,
            String timezone
    ) {
    }

    public record DailyWeather(
            LocalDate date,
            int weatherCode,
            double temperatureMax,
            double temperatureMin,
            int precipitationProbability,
            double precipitationMm,
            double windMaxKmh
    ) {
    }

    public record WeatherReport(
            GeoLocation location,
            List<DailyWeather> days,
            List<String> advisories,
            Instant fetchedAt
    ) {

        public WeatherReport {
            days = List.copyOf(days);
            advisories = List.copyOf(advisories);
        }
    }
}
