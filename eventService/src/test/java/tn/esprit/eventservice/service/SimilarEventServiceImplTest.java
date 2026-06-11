package tn.esprit.eventservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventservice.dto.SimilarEventDTO;
import tn.esprit.eventservice.entity.*;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.EventRatingRepository;
import tn.esprit.eventservice.repository.EventRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SimilarEventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventRatingRepository ratingRepository;

    @InjectMocks
    private SimilarEventServiceImpl similarEventService;

    private Event buildEvent(Long id, Long categoryId, Double lat, Double lon, LocalDateTime startDate) {
        Category cat = new Category();
        cat.setId(categoryId);
        cat.setName("Tech");

        Event e = new Event();
        e.setId(id);
        e.setName("Event " + id);
        e.setLocation("Tunis");
        e.setLatitude(lat);
        e.setLongitude(lon);
        e.setStartDate(startDate);
        e.setEndDate(startDate.plusHours(2));
        e.setMaxCapacity(100);
        e.setCurrentParticipants(0);
        e.setStatus(EventStatus.APPROVED);
        e.setCategory(cat);
        e.setUserId(1L);
        e.setPrice(BigDecimal.ZERO);
        e.setEventType(EventType.OUTDOOR);
        return e;
    }

    @Test
    @DisplayName("getSimilarEvents_shouldReturnList_whenCandidatesExist")
    void getSimilarEvents_shouldReturnList_whenCandidatesExist() {
        LocalDateTime now = LocalDateTime.now().plusDays(5);
        Event source = buildEvent(1L, 1L, 36.8, 10.18, now);
        Event candidate = buildEvent(2L, 1L, 36.81, 10.19, now.plusDays(1));

        given(eventRepository.findById(1L)).willReturn(Optional.of(source));
        given(eventRepository.findSimilarEvents(eq(1L), eq(1L), anyString(), any(), any()))
                .willReturn(List.of(candidate));
        given(ratingRepository.findAverageRatingByEventId(2L)).willReturn(4.2);

        List<SimilarEventDTO> result = similarEventService.getSimilarEvents(1L, 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(0).getAverageRating()).isEqualTo(4.2);
    }

    @Test
    @DisplayName("getSimilarEvents_shouldThrowResourceNotFound_whenEventDoesNotExist")
    void getSimilarEvents_shouldThrowResourceNotFound_whenEventDoesNotExist() {
        given(eventRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> similarEventService.getSimilarEvents(999L, 5))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("getSimilarEvents_shouldReturnEmptyList_whenNoCandidates")
    void getSimilarEvents_shouldReturnEmptyList_whenNoCandidates() {
        LocalDateTime now = LocalDateTime.now().plusDays(5);
        Event source = buildEvent(1L, 1L, 36.8, 10.18, now);

        given(eventRepository.findById(1L)).willReturn(Optional.of(source));
        given(eventRepository.findSimilarEvents(eq(1L), eq(1L), anyString(), any(), any()))
                .willReturn(Collections.emptyList());

        List<SimilarEventDTO> result = similarEventService.getSimilarEvents(1L, 5);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getSimilarEvents_shouldRespectLimit_whenCandidatesExceedLimit")
    void getSimilarEvents_shouldRespectLimit_whenCandidatesExceedLimit() {
        LocalDateTime now = LocalDateTime.now().plusDays(5);
        Event source = buildEvent(1L, 1L, 36.8, 10.18, now);

        List<Event> candidates = List.of(
                buildEvent(2L, 1L, 36.81, 10.19, now),
                buildEvent(3L, 1L, 36.82, 10.20, now),
                buildEvent(4L, 1L, 36.83, 10.21, now),
                buildEvent(5L, 1L, 36.84, 10.22, now)
        );

        given(eventRepository.findById(1L)).willReturn(Optional.of(source));
        given(eventRepository.findSimilarEvents(eq(1L), eq(1L), anyString(), any(), any()))
                .willReturn(candidates);
        given(ratingRepository.findAverageRatingByEventId(anyLong())).willReturn(null);

        List<SimilarEventDTO> result = similarEventService.getSimilarEvents(1L, 2);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getSimilarEvents_shouldHandleNullCategoryOnSource")
    void getSimilarEvents_shouldHandleNullCategoryOnSource() {
        LocalDateTime now = LocalDateTime.now().plusDays(5);
        Event source = buildEvent(1L, 1L, 36.8, 10.18, now);
        source.setCategory(null); // null category

        given(eventRepository.findById(1L)).willReturn(Optional.of(source));
        given(eventRepository.findSimilarEvents(eq(1L), eq(-1L), anyString(), any(), any()))
                .willReturn(Collections.emptyList());

        List<SimilarEventDTO> result = similarEventService.getSimilarEvents(1L, 5);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getSimilarEvents_shouldSetDefaultRating_whenRatingIsNull")
    void getSimilarEvents_shouldSetDefaultRating_whenRatingIsNull() {
        LocalDateTime now = LocalDateTime.now().plusDays(5);
        Event source = buildEvent(1L, 1L, 36.8, 10.18, now);
        Event candidate = buildEvent(2L, 1L, 36.81, 10.19, now);

        given(eventRepository.findById(1L)).willReturn(Optional.of(source));
        given(eventRepository.findSimilarEvents(any(), any(), any(), any(), any()))
                .willReturn(List.of(candidate));
        given(ratingRepository.findAverageRatingByEventId(2L)).willReturn(null);

        List<SimilarEventDTO> result = similarEventService.getSimilarEvents(1L, 5);

        assertThat(result.get(0).getAverageRating()).isEqualTo(0.0);
    }
}