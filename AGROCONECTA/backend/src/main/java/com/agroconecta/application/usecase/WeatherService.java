package com.agroconecta.application.usecase;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.port.in.WeatherUseCase;
import com.agroconecta.application.port.out.CropRepositoryPort;
import com.agroconecta.application.port.out.ParcelRepositoryPort;
import com.agroconecta.application.port.out.WeatherProviderPort;
import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.exception.ServiceUnavailableException;
import com.agroconecta.domain.model.Crop;
import com.agroconecta.domain.model.CropStatus;
import com.agroconecta.domain.model.Parcel;
import com.agroconecta.domain.model.Weather;
import com.agroconecta.domain.service.WeatherAdvisor;

import java.time.Instant;
import java.util.List;

public class WeatherService implements WeatherUseCase {

    private final WeatherProviderPort provider;
    private final ParcelRepositoryPort parcelRepository;
    private final CropRepositoryPort cropRepository;
    private final WeatherAdvisor advisor;

    public WeatherService(WeatherProviderPort provider, ParcelRepositoryPort parcelRepository,
                          CropRepositoryPort cropRepository, WeatherAdvisor advisor) {
        this.provider = provider;
        this.parcelRepository = parcelRepository;
        this.cropRepository = cropRepository;
        this.advisor = advisor;
    }

    @Override
    public Weather.WeatherReport report(String location) {
        String query = normalize(location);
        Weather.GeoLocation geo = provider.geocode(query)
                .orElseThrow(() -> new NotFoundException("No encontramos la ubicación indicada"));
        List<Weather.DailyWeather> days = forecast(geo);
        return new Weather.WeatherReport(geo, days, advisor.advise(days, List.of()), Instant.now());
    }

    @Override
    public Weather.WeatherReport reportForParcel(Long ownerId, Long parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new NotFoundException("La parcela no existe"));
        if (!parcel.belongsTo(ownerId)) {
            throw new ForbiddenException("No puedes consultar el clima de una parcela ajena");
        }
        Weather.GeoLocation geo = provider.geocode(parcel.location())
                .orElseThrow(() -> new NotFoundException(
                        "No pudimos ubicar la parcela. Actualiza su ubicación con municipio y departamento"));
        List<Weather.DailyWeather> days = forecast(geo);
        List<String> crops = cropRepository.findByOwnerId(ownerId, parcelId, PageQuery.of(0, 100)).content()
                .stream()
                .filter(crop -> crop.status() != CropStatus.COSECHADO && crop.status() != CropStatus.PERDIDO)
                .map(Crop::name)
                .distinct()
                .toList();
        return new Weather.WeatherReport(geo, days, advisor.advise(days, crops), Instant.now());
    }

    private List<Weather.DailyWeather> forecast(Weather.GeoLocation geo) {
        List<Weather.DailyWeather> days = provider.forecast(geo.latitude(), geo.longitude());
        if (days.isEmpty()) {
            throw new ServiceUnavailableException(
                    "El servicio de clima no devolvió datos. Intenta de nuevo más tarde.");
        }
        return days;
    }

    private String normalize(String location) {
        if (location == null || location.isBlank()) {
            throw new BusinessRuleException("Escribe un municipio o ciudad para consultar el clima");
        }
        String trimmed = location.trim();
        if (trimmed.length() > 120) {
            throw new BusinessRuleException("La ubicación no puede superar 120 caracteres");
        }
        return trimmed;
    }
}
