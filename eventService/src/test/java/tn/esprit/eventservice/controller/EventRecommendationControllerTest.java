package tn.esprit.eventservice.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tn.esprit.eventservice.dto.EventDTO;
import tn.esprit.eventservice.dto.PagedResponse;
import tn.esprit.eventservice.dto.ScoredEventDTO;
import tn.esprit.eventservice.service.RecommendationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EventRecommendationControllerTest {

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private EventRecommendationController recommendationController;

    @Test
    @DisplayName("getRecommendedEvents_shouldReturn200_withPagedResponse")
    void getRecommendedEvents_shouldReturn200_withPagedResponse() {
        // Given
        EventDTO eventDTO = new EventDTO();
        eventDTO.setId(1L);
        eventDTO.setName("Concert Tunis");

        ScoredEventDTO scored = new ScoredEventDTO(eventDTO, 0.9);
        Page<ScoredEventDTO> page = new PageImpl<>(List.of(scored), PageRequest.of(0, 9), 1);

        given(recommendationService.getRecommendedEvents(1L, PageRequest.of(0, 9))).willReturn(page);

        // When
        ResponseEntity<PagedResponse<ScoredEventDTO>> result =
                recommendationController.getRecommendedEvents(1L, 0, 9);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).hasSize(1);
        assertThat(result.getBody().getTotalElements()).isEqualTo(1);
        assertThat(result.getBody().isLast()).isTrue();
        assertThat(result.getBody().getContent().get(0).getScoreLabel()).isEqualTo("Top pick");
    }

    @Test
    @DisplayName("getRecommendedEvents_shouldReturnEmptyPage_whenNoRecommendations")
    void getRecommendedEvents_shouldReturnEmptyPage_whenNoRecommendations() {
        // Given
        Page<ScoredEventDTO> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 9), 0);
        given(recommendationService.getRecommendedEvents(99L, PageRequest.of(0, 9))).willReturn(emptyPage);

        // When
        ResponseEntity<PagedResponse<ScoredEventDTO>> result =
                recommendationController.getRecommendedEvents(99L, 0, 9);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).isEmpty();
        assertThat(result.getBody().getTotalElements()).isZero();
    }
}