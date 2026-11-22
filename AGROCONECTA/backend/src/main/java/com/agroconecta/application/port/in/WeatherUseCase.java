package com.agroconecta.application.port.in;

import com.agroconecta.domain.model.Weather;

public interface WeatherUseCase {

    Weather.WeatherReport report(String location);

    Weather.WeatherReport reportForParcel(Long ownerId, Long parcelId);
}
