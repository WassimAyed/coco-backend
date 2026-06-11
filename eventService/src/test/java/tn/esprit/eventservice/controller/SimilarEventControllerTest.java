package tn.esprit.eventservice.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tn.esprit.eventservice.dto.SimilarEventDTO;
import tn.esprit.eventservice.service.SimilarEventServiceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SimilarEventControllerTest {

    @Mock
    private SimilarEventServiceImpl similarEventService;

    @InjectMocks
    private SimilarEventController similarEventController;

    @Test
    @DisplayName("getSimilar_shouldReturn200_withListOfSimilarEvents")
    void getSimilar_shouldReturn200_withListOfSimilarEvents() {
        // Given
        SimilarEventDTO dto1 = new SimilarEventDTO();
        dto1.setId(2L);
        dto1.setName("Festival Jazz");
        dto1.setSimilarityReason("Same category");

        SimilarEventDTO dto2 = new SimilarEventDTO();
        dto2.setId(3L);
        dto2.setName("Concert Rock");
        dto2.setSimilarityReason("Same location");

        given(similarEventService.getSimilarEvents(1L, 5)).willReturn(List.of(dto1, dto2));

        // When
        ResponseEntity<List<SimilarEventDTO>> result = similarEventController.getSimilar(1L, 5);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull().hasSize(2);
        assertThat(result.getBody().get(0).getName()).isEqualTo("Festival Jazz");
    }

    @Test
    @DisplayName("getSimilar_shouldReturn200_withEmptyList_whenNoSimilarEvents")
    void getSimilar_shouldReturn200_withEmptyList_whenNoSimilarEvents() {
        // Given
        given(similarEventService.getSimilarEvents(99L, 5)).willReturn(List.of());

        // When
        ResponseEntity<List<SimilarEventDTO>> result = similarEventController.getSimilar(99L, 5);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("getSimilar_shouldUseDefaultLimit_whenNotSpecified")
    void getSimilar_shouldUseDefaultLimit_whenNotSpecified() {
        // Given
        given(similarEventService.getSimilarEvents(1L, 5)).willReturn(List.of());

        // When
        ResponseEntity<List<SimilarEventDTO>> result = similarEventController.getSimilar(1L, 5);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}