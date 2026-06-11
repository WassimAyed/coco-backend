package tn.esprit.eventservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class WeatherServiceTest {

    private WeatherService weatherService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService();
        restTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(weatherService, "restTemplate", restTemplate);
    }

    private String buildOpenMeteoResponse(int weatherCode, double temp, double precip, double wind) {
        return String.format("""
                {
                  "daily": {
                    "weather_code": [%d],
                    "temperature_2m_mean": [%s],
                    "precipitation_sum": [%s],
                    "wind_speed_10m_max": [%s]
                  }
                }
                """, weatherCode, temp, precip, wind);
    }

    @Test
    @DisplayName("getDailyWeather_shouldReturnSnapshot_whenApiResponds")
    void getDailyWeather_shouldReturnSnapshot_whenApiResponds() {
        given(restTemplate.getForObject(anyString(), eq(String.class)))
                .willReturn(buildOpenMeteoResponse(0, 24.5, 0.0, 15.0));

        WeatherService.WeatherSnapshot result =
                weatherService.getDailyWeather(36.8, 10.18, LocalDate.now().plusDays(3));

        assertThat(result).isNotNull();
        assertThat(result.getWeatherCode()).isZero();
        assertThat(result.getWeatherLabel()).isEqualTo("Clear");
        assertThat(result.getTemperature()).isEqualTo(24.5);
        assertThat(result.getPrecipitationMm()).isEqualTo(0.0);
        assertThat(result.getWindSpeedKmh()).isEqualTo(15.0);
    }

    @Test
    @DisplayName("getDailyWeather_shouldReturnNull_whenLatitudeIsNull")
    void getDailyWeather_shouldReturnNull_whenLatitudeIsNull() {
        WeatherService.WeatherSnapshot result =
                weatherService.getDailyWeather(null, 10.18, LocalDate.now());
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getDailyWeather_shouldReturnNull_whenDateIsNull")
    void getDailyWeather_shouldReturnNull_whenDateIsNull() {
        WeatherService.WeatherSnapshot result =
                weatherService.getDailyWeather(36.8, 10.18, null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getDailyWeather_shouldReturnNull_whenApiThrowsException")
    void getDailyWeather_shouldReturnNull_whenApiThrowsException() {
        given(restTemplate.getForObject(anyString(), eq(String.class)))
                .willThrow(new RuntimeException("API timeout"));

        WeatherService.WeatherSnapshot result =
                weatherService.getDailyWeather(36.8, 10.18, LocalDate.now().plusDays(1));

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getDailyWeather_shouldReturnNull_whenDailyNodeIsMissing")
    void getDailyWeather_shouldReturnNull_whenDailyNodeIsMissing() {
        given(restTemplate.getForObject(anyString(), eq(String.class)))
                .willReturn("{\"hourly\": {}}");

        WeatherService.WeatherSnapshot result =
                weatherService.getDailyWeather(36.8, 10.18, LocalDate.now().plusDays(1));

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("mapWeatherCode_shouldMapKnownCodes_correctly")
    void mapWeatherCode_shouldMapKnownCodes_correctly() {
        assertThat(weatherService.mapWeatherCode(0)).isEqualTo("Clear");
        assertThat(weatherService.mapWeatherCode(1)).isEqualTo("Clouds");
        assertThat(weatherService.mapWeatherCode(45)).isEqualTo("Fog");
        assertThat(weatherService.mapWeatherCode(61)).isEqualTo("Rain");
        assertThat(weatherService.mapWeatherCode(71)).isEqualTo("Snow");
        assertThat(weatherService.mapWeatherCode(95)).isEqualTo("Thunderstorm");
        assertThat(weatherService.mapWeatherCode(999)).isEqualTo("Unknown");
    }

    @Test
    @DisplayName("getAverageDailyWeather_shouldReturnSnapshot_whenMultipleDays")
    void getAverageDailyWeather_shouldReturnSnapshot_whenMultipleDays() {
        String multiDayResponse = """
                {
                  "daily": {
                    "weather_code": [1, 2],
                    "temperature_2m_mean": [22.0, 24.0],
                    "precipitation_sum": [1.0, 3.0],
                    "wind_speed_10m_max": [10.0, 20.0]
                  }
                }
                """;

        given(restTemplate.getForObject(anyString(), eq(String.class)))
                .willReturn(multiDayResponse);

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = LocalDate.now().plusDays(2);

        WeatherService.WeatherSnapshot result =
                weatherService.getAverageDailyWeather(36.8, 10.18, start, end);

        assertThat(result).isNotNull();
        assertThat(result.getTemperature()).isEqualTo(23.0);     // (22+24)/2
        assertThat(result.getWindSpeedKmh()).isEqualTo(15.0);    // (10+20)/2
        assertThat(result.getPrecipitationMm()).isEqualTo(4.0);  // somme: 1+3
    }

    @Test
    @DisplayName("getAverageDailyWeather_shouldReturnNull_whenLatIsNull")
    void getAverageDailyWeather_shouldReturnNull_whenLatIsNull() {
        WeatherService.WeatherSnapshot result =
                weatherService.getAverageDailyWeather(null, 10.18,
                        LocalDate.now(), LocalDate.now().plusDays(2));
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getAverageDailyWeather_shouldFallbackToDailyWeather_whenEndDateIsNull")
    void getAverageDailyWeather_shouldFallbackToDailyWeather_whenEndDateIsNull() {
        given(restTemplate.getForObject(anyString(), eq(String.class)))
                .willReturn(buildOpenMeteoResponse(3, 20.0, 0.5, 12.0));

        LocalDate start = LocalDate.now().plusDays(2);

        WeatherService.WeatherSnapshot result =
                weatherService.getAverageDailyWeather(36.8, 10.18, start, null);

        assertThat(result).isNotNull();
        assertThat(result.getWeatherLabel()).isEqualTo("Clouds");
    }
}