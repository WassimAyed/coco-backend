package tn.esprit.eventservice.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tn.esprit.eventservice.dto.EventRatingDTO;
import tn.esprit.eventservice.service.EventRatingServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventRatingControllerTest {

    @Mock
    private EventRatingServiceImpl ratingService;

    @InjectMocks
    private EventRatingController ratingController;

    @Test
    @DisplayName("rate_shouldReturn201_whenDtoIsValid")
    void rate_shouldReturn201_whenDtoIsValid() {
        // Given
        EventRatingDTO request = EventRatingDTO.builder()
                .eventId(1L).userId(2L).rating(4).build();
        EventRatingDTO response = EventRatingDTO.builder()
                .id(10L).eventId(1L).userId(2L).rating(4).averageRating(4.0).totalRatings(1L).build();
        given(ratingService.rateEvent(request)).willReturn(response);

        // When
        ResponseEntity<EventRatingDTO> result = ratingController.rate(request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(10L);
        assertThat(result.getBody().getRating()).isEqualTo(4);
    }

    @Test
    @DisplayName("getStats_shouldReturn200_withAverageAndTotal")
    void getStats_shouldReturn200_withAverageAndTotal() {
        // Given
        EventRatingDTO stats = EventRatingDTO.builder()
                .eventId(1L).averageRating(3.5).totalRatings(4L).build();
        given(ratingService.getEventRatingStats(1L)).willReturn(stats);

        // When
        ResponseEntity<EventRatingDTO> result = ratingController.getStats(1L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getAverageRating()).isEqualTo(3.5);
        assertThat(result.getBody().getTotalRatings()).isEqualTo(4L);
    }

    @Test
    @DisplayName("getUserRating_shouldReturn200_whenRatingExists")
    void getUserRating_shouldReturn200_whenRatingExists() {
        // Given
        EventRatingDTO userRating = EventRatingDTO.builder()
                .eventId(1L).userId(2L).rating(5).build();
        given(ratingService.getUserRating(1L, 2L)).willReturn(userRating);

        // When
        ResponseEntity<EventRatingDTO> result = ratingController.getUserRating(1L, 2L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("delete_shouldReturn204_whenRatingExists")
    void delete_shouldReturn204_whenRatingExists() {
        // When
        ResponseEntity<Void> result = ratingController.delete(1L, 2L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(ratingService).deleteRating(1L, 2L);
    }
}