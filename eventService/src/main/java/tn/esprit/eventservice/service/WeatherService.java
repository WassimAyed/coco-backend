package tn.esprit.eventservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

/**
 * Service météo basé sur Open-Meteo Forecast (daily).
 *
 * Endpoint utilisé:
 *   GET https://api.open-meteo.com/v1/forecast
 *     ?latitude={lat}
 *     &longitude={lon}
 *     &daily=weather_code,temperature_2m_mean,precipitation_sum,wind_speed_10m_max
 *     &timezone=auto
 *     &start_date={date}
 *     &end_date={date}
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private static final String OPEN_METEO_DAILY_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&daily=weather_code,temperature_2m_mean,precipitation_sum,wind_speed_10m_max&timezone=auto&start_date=%s&end_date=%s";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherSnapshot getDailyWeather(Double latitude, Double longitude, LocalDate eventDate) {
        if (latitude == null || longitude == null || eventDate == null) {
            return null;
        }

        String date = eventDate.toString();
        String url = String.format(OPEN_METEO_DAILY_URL, latitude, longitude, date, date);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode daily = root.path("daily");

            if (daily.isMissingNode()) {
                return null;
            }

            JsonNode weatherCodes = daily.path("weather_code");
                JsonNode temperatures = daily.path("temperature_2m_mean");
                JsonNode precipitation = daily.path("precipitation_sum");
                JsonNode windSpeed = daily.path("wind_speed_10m_max");

            if (!weatherCodes.isArray() || weatherCodes.isEmpty()
                    || !temperatures.isArray() || temperatures.isEmpty()
                    || !precipitation.isArray() || precipitation.isEmpty()
                    || !windSpeed.isArray() || windSpeed.isEmpty()) {
                return null;
            }

            int weatherCode = weatherCodes.get(0).asInt();

            WeatherSnapshot snapshot = new WeatherSnapshot();
            snapshot.setWeatherCode(weatherCode);
            snapshot.setWeatherLabel(mapWeatherCode(weatherCode));
                snapshot.setTemperature(roundOneDecimal(temperatures.get(0).asDouble()));
            snapshot.setPrecipitationMm(roundTwoDecimals(precipitation.get(0).asDouble()));
                snapshot.setWindSpeedKmh(roundOneDecimal(windSpeed.get(0).asDouble()));
            return snapshot;

        } catch (Exception e) {
            // We never block event creation because of weather fetch issues.
            log.warn("Open-Meteo request failed", e);
            return null;
        }
    }

    public WeatherSnapshot getAverageDailyWeather(Double latitude, Double longitude, LocalDate startDate, LocalDate endDate) {
        if (latitude == null || longitude == null || startDate == null) {
            return null;
        }

        if (endDate == null || endDate.equals(startDate)) {
            return getDailyWeather(latitude, longitude, startDate);
        }

        LocalDate effectiveEndDate = endDate;
        if (effectiveEndDate.isBefore(startDate)) {
            effectiveEndDate = startDate;
        }

        LocalDate maxEndDate = startDate.plusDays(6);
        if (effectiveEndDate.isAfter(maxEndDate)) {
            effectiveEndDate = maxEndDate;
        }

        String url = String.format(OPEN_METEO_DAILY_URL, latitude, longitude, startDate, effectiveEndDate);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode daily = root.path("daily");

            if (daily.isMissingNode()) {
                return null;
            }

            JsonNode weatherCodes = daily.path("weather_code");
            JsonNode temperatures = daily.path("temperature_2m_mean");
            JsonNode precipitation = daily.path("precipitation_sum");
            JsonNode windSpeed = daily.path("wind_speed_10m_max");

            if (!weatherCodes.isArray() || weatherCodes.isEmpty()
                    || !temperatures.isArray() || temperatures.isEmpty()
                    || !precipitation.isArray() || precipitation.isEmpty()
                    || !windSpeed.isArray() || windSpeed.isEmpty()) {
                return null;
            }

            int valuesCount = Math.min(
                    temperatures.size(),
                    Math.min(precipitation.size(), windSpeed.size())
            );
            if (valuesCount <= 0) {
                return null;
            }

            double temperatureSum = 0.0;
            double precipitationSum = 0.0;
            double windSpeedSum = 0.0;

            for (int i = 0; i < valuesCount; i++) {
                temperatureSum += temperatures.get(i).asDouble();
                precipitationSum += precipitation.get(i).asDouble();
                windSpeedSum += windSpeed.get(i).asDouble();
            }

            int weatherCode = weatherCodes.get(0).asInt();

            WeatherSnapshot snapshot = new WeatherSnapshot();
            snapshot.setWeatherCode(weatherCode);
            snapshot.setWeatherLabel(mapWeatherCode(weatherCode));
            snapshot.setTemperature(roundOneDecimal(temperatureSum / valuesCount));
            snapshot.setPrecipitationMm(roundTwoDecimals(precipitationSum)); // somme totale sur la période
            snapshot.setWindSpeedKmh(roundOneDecimal(windSpeedSum / valuesCount));
            return snapshot;
        } catch (Exception e) {
            log.warn("Open-Meteo average weather request failed", e);
            return null;
        }
    }

    public String mapWeatherCode(int weatherCode) {
        return switch (weatherCode) {
            case 0 -> "Clear";
            case 1, 2, 3 -> "Clouds";
            case 45, 48 -> "Fog";
            case 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> "Rain";
            case 71, 73, 75, 77, 85, 86 -> "Snow";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Unknown";
        };
    }

    public static class WeatherSnapshot {
        private Double temperature;
        private Double precipitationMm;
        private Double windSpeedKmh;
        private Integer weatherCode;
        private String weatherLabel;

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getPrecipitationMm() {
            return precipitationMm;
        }

        public void setPrecipitationMm(Double precipitationMm) {
            this.precipitationMm = precipitationMm;
        }

        public Double getWindSpeedKmh() {
            return windSpeedKmh;
        }

        public void setWindSpeedKmh(Double windSpeedKmh) {
            this.windSpeedKmh = windSpeedKmh;
        }

        public Integer getWeatherCode() {
            return weatherCode;
        }

        public void setWeatherCode(Integer weatherCode) {
            this.weatherCode = weatherCode;
        }

        public String getWeatherLabel() {
            return weatherLabel;
        }

        public void setWeatherLabel(String weatherLabel) {
            this.weatherLabel = weatherLabel;
        }
    }

    private Double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private Double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
