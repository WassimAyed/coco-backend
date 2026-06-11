package tn.esprit.eventservice.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.esprit.eventservice.entity.EventStatus;
import tn.esprit.eventservice.entity.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventDtoTest {

    @Test
    @DisplayName("builder_shouldSetCoreFields_whenValuesProvided")
    void builder_shouldSetCoreFields_whenValuesProvided() {
        // Given
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);

        // When
        EventDTO dto = EventDTO.builder()
                .id(1L)
                .name("Conference")
                .location("Tunis")
                .startDate(start)
                .endDate(end)
                .maxCapacity(200)
                .status(EventStatus.PENDING)
                .categoryId(3L)
                .eventType(EventType.INDOOR)
                .price(BigDecimal.valueOf(50))
                .build();

        // Then
        assertThat(dto.getName()).isEqualTo("Conference");
    }

    @Test
    @DisplayName("setters_shouldUpdateWeatherFields_whenDtoIsMutable")
    void setters_shouldUpdateWeatherFields_whenDtoIsMutable() {
        // Given
        EventDTO dto = new EventDTO();

        // When
        dto.setTemperature(25.3);
        dto.setPrecipitationMm(0.2);
        dto.setWindSpeedKmh(13.4);
        dto.setWeatherCode(3);
        dto.setWeatherLabel("Clouds");

        // Then
        assertThat(dto.getWeatherLabel()).isEqualTo("Clouds");
    }
}
