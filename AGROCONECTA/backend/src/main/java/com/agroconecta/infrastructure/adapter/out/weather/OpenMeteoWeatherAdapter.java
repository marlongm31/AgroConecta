package com.agroconecta.infrastructure.adapter.out.weather;

import com.agroconecta.application.port.out.WeatherProviderPort;
import com.agroconecta.domain.exception.ServiceUnavailableException;
import com.agroconecta.domain.model.Weather;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OpenMeteoWeatherAdapter implements WeatherProviderPort {

    private final RestClient restClient;
    private final String geocodingUrl;
    private final String forecastUrl;
    private final Duration cacheTtl;
    private final ConcurrentHashMap<String, CacheEntry<Weather.GeoLocation>> geocodeCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CacheEntry<List<Weather.DailyWeather>>> forecastCache =
            new ConcurrentHashMap<>();

    public OpenMeteoWeatherAdapter(@Value("${app.weather.geocoding-url}") String geocodingUrl,
                                   @Value("${app.weather.forecast-url}") String forecastUrl,
                                   @Value("${app.weather.cache-ttl-seconds:900}") long cacheTtlSeconds,
                                   @Value("${app.weather.connect-timeout-ms:5000}") int connectTimeoutMs,
                                   @Value("${app.weather.read-timeout-ms:8000}") int readTimeoutMs) {
        this.geocodingUrl = geocodingUrl;
        this.forecastUrl = forecastUrl;
        this.cacheTtl = Duration.ofSeconds(cacheTtlSeconds);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public Optional<Weather.GeoLocation> geocode(String query) {
        String key = query.toLowerCase();
        Optional<Weather.GeoLocation> cached = readCache(geocodeCache, key);
        if (cached.isPresent()) {
            return cached;
        }
        try {
            JsonNode response = restClient.get()
                    .uri(geocodingUrl + "?name={name}&count=1&language=es&format=json", query)
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode results = response == null ? null : response.get("results");
            if (results == null || !results.isArray() || results.isEmpty()) {
                return Optional.empty();
            }
            JsonNode first = results.get(0);
            Weather.GeoLocation location = new Weather.GeoLocation(
                    first.path("name").asText(),
                    first.path("admin1").asText(null),
                    first.path("country").asText(null),
                    first.path("latitude").asDouble(),
                    first.path("longitude").asDouble(),
                    first.path("timezone").asText("auto"));
            writeCache(geocodeCache, key, location);
            return Optional.of(location);
        } catch (RestClientException exception) {
            throw new ServiceUnavailableException(
                    "No pudimos consultar el servicio de clima. Verifica tu conexión e intenta de nuevo.", exception);
        }
    }

    @Override
    public List<Weather.DailyWeather> forecast(double latitude, double longitude) {
        String key = String.format("%.3f,%.3f", latitude, longitude);
        Optional<List<Weather.DailyWeather>> cached = readCache(forecastCache, key);
        if (cached.isPresent()) {
            return cached.get();
        }
        try {
            JsonNode response = restClient.get()
                    .uri(forecastUrl + "?latitude={lat}&longitude={lon}"
                                    + "&daily=weather_code,temperature_2m_max,temperature_2m_min,"
                                    + "precipitation_sum,precipitation_probability_max,wind_speed_10m_max"
                                    + "&timezone=auto&forecast_days=7",
                            latitude, longitude)
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode daily = response == null ? null : response.get("daily");
            if (daily == null) {
                return List.of();
            }
            JsonNode dates = daily.get("time");
            if (dates == null || !dates.isArray()) {
                return List.of();
            }
            List<Weather.DailyWeather> days = new ArrayList<>();
            for (int index = 0; index < dates.size(); index++) {
                days.add(new Weather.DailyWeather(
                        LocalDate.parse(dates.get(index).asText()),
                        daily.path("weather_code").path(index).asInt(),
                        daily.path("temperature_2m_max").path(index).asDouble(),
                        daily.path("temperature_2m_min").path(index).asDouble(),
                        daily.path("precipitation_probability_max").path(index).asInt(),
                        daily.path("precipitation_sum").path(index).asDouble(),
                        daily.path("wind_speed_10m_max").path(index).asDouble()));
            }
            if (!days.isEmpty()) {
                writeCache(forecastCache, key, List.copyOf(days));
            }
            return days;
        } catch (RestClientException exception) {
            throw new ServiceUnavailableException(
                    "No pudimos consultar el pronóstico. Verifica tu conexión e intenta de nuevo.", exception);
        } catch (RuntimeException exception) {
            throw new ServiceUnavailableException(
                    "El servicio de clima devolvió una respuesta inesperada. Intenta de nuevo más tarde.", exception);
        }
    }

    private <T> Optional<T> readCache(ConcurrentHashMap<String, CacheEntry<T>> cache, String key) {
        CacheEntry<T> entry = cache.get(key);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            cache.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    private <T> void writeCache(ConcurrentHashMap<String, CacheEntry<T>> cache, String key, T value) {
        cache.put(key, new CacheEntry<>(value, Instant.now().plus(cacheTtl)));
    }

    private record CacheEntry<T>(T value, Instant expiresAt) {
    }
}
