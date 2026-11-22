package com.agroconecta.application.port.out;

import com.agroconecta.domain.model.Weather;

import java.util.List;
import java.util.Optional;

public interface WeatherProviderPort {

    Optional<Weather.GeoLocation> geocode(String query);

    List<Weather.DailyWeather> forecast(double latitude, double longitude);
}
