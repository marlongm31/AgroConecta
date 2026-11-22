package com.agroconecta.domain.service;

import com.agroconecta.domain.model.Weather;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class WeatherAdvisor {

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM");

    public List<String> advise(List<Weather.DailyWeather> days, List<String> crops) {
        Set<String> advisories = new LinkedHashSet<>();
        String cropText = crops == null || crops.isEmpty()
                ? "tus parcelas"
                : "los cultivos de " + String.join(", ", crops);
        for (Weather.DailyWeather day : days) {
            String date = day.date().format(DAY_FORMAT);
            if (day.precipitationMm() >= 30) {
                advisories.add("El " + date + " se esperan lluvias intensas (" + Math.round(day.precipitationMm())
                        + " mm): revisa drenajes y evita fertilizar en " + cropText + ".");
            } else if (day.precipitationMm() >= 10) {
                advisories.add("El " + date + " hay probabilidad de lluvia: considera posponer la aplicación de insumos en "
                        + cropText + ".");
            }
            if (day.temperatureMin() <= 4) {
                advisories.add("El " + date + " hay riesgo de helada (" + Math.round(day.temperatureMin())
                        + " °C): protege " + cropText + ".");
            }
            if (day.temperatureMax() >= 35) {
                advisories.add("El " + date + " se prevé calor extremo (" + Math.round(day.temperatureMax())
                        + " °C): refuerza el riego y evita labores al mediodía en " + cropText + ".");
            }
            if (day.windMaxKmh() >= 45) {
                advisories.add("El " + date + " se esperan vientos fuertes (" + Math.round(day.windMaxKmh())
                        + " km/h): asegura tutores y evita aspersiones en " + cropText + ".");
            }
            if (day.precipitationProbability() <= 20 && day.temperatureMax() >= 30) {
                advisories.add("El " + date + " será seco y caluroso: verifica la humedad del suelo en " + cropText + ".");
            }
            if (day.weatherCode() >= 95) {
                advisories.add("El " + date + " hay posibilidad de tormenta eléctrica: no realices labores en campo abierto.");
            }
            if (day.weatherCode() == 45 || day.weatherCode() == 48) {
                advisories.add("El " + date + " se prevé niebla: retrasa las aspersiones hasta que mejore la visibilidad.");
            }
        }
        return new ArrayList<>(advisories).stream().limit(8).toList();
    }

    public String conditionLabel(int code) {
        if (code == 0) {
            return "Despejado";
        }
        if (code <= 3) {
            return "Parcialmente nublado";
        }
        if (code == 45 || code == 48) {
            return "Niebla";
        }
        if (code >= 51 && code <= 57) {
            return "Llovizna";
        }
        if (code >= 61 && code <= 67) {
            return "Lluvia";
        }
        if (code >= 71 && code <= 77) {
            return "Nieve";
        }
        if (code >= 80 && code <= 82) {
            return "Chubascos";
        }
        if (code >= 95) {
            return "Tormenta";
        }
        return "Nublado";
    }
}
