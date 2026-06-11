package tn.esprit.eventservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {

    private GeocodingService geocodingService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        geocodingService = new GeocodingService();
        restTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(geocodingService, "restTemplate", restTemplate);
    }

    @Test
    @DisplayName("getCoordinates_shouldReturnLatLon_whenAddressIsFound")
    void getCoordinates_shouldReturnLatLon_whenAddressIsFound() {
        String nominatimResponse = """
                [{"lat":"36.8","lon":"10.18","display_name":"Tunis, Tunisie"}]
                """;

        given(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))
        ).willReturn(new ResponseEntity<>(nominatimResponse, HttpStatus.OK));

        Map<String, String> result = geocodingService.getCoordinates("Tunis");

        assertThat(result).containsEntry("lat", "36.8")
                .containsEntry("lon", "10.18")
                .containsEntry("display_name", "Tunis, Tunisie");
    }

    @Test
    @DisplayName("getCoordinates_shouldReturnEmptyMap_whenAddressNotFound")
    void getCoordinates_shouldReturnEmptyMap_whenAddressNotFound() {
        given(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))
        ).willReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        Map<String, String> result = geocodingService.getCoordinates("Unknown Place XYZ");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCoordinates_shouldReturnEmptyMap_whenAddressIsNull")
    void getCoordinates_shouldReturnEmptyMap_whenAddressIsNull() {
        Map<String, String> result = geocodingService.getCoordinates(null);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCoordinates_shouldReturnEmptyMap_whenAddressIsBlank")
    void getCoordinates_shouldReturnEmptyMap_whenAddressIsBlank() {
        Map<String, String> result = geocodingService.getCoordinates("   ");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCoordinates_shouldReturnEmptyMap_whenRestTemplateThrows")
    void getCoordinates_shouldReturnEmptyMap_whenRestTemplateThrows() {
        given(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))
        ).willThrow(new RuntimeException("Network error"));

        Map<String, String> result = geocodingService.getCoordinates("Ariana, Tunisie");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCoordinates_shouldReturnEmptyMap_whenResponseBodyIsInvalidJson")
    void getCoordinates_shouldReturnEmptyMap_whenResponseBodyIsInvalidJson() {
        given(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))
        ).willReturn(new ResponseEntity<>("NOT_JSON", HttpStatus.OK));

        Map<String, String> result = geocodingService.getCoordinates("Sousse");

        assertThat(result).isEmpty();
    }
}