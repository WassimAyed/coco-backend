package tn.esprit.eventservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import tn.esprit.eventservice.dto.ScoredEventDTO;
import tn.esprit.eventservice.entity.*;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.repository.UserBehaviorRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserBehaviorRepository userBehaviorRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private Event buildEvent(Long id, Long categoryId, EventType type, BigDecimal price) {
        Category cat = new Category();
        cat.setId(categoryId);
        cat.setName("Tech");

        Event e = new Event();
        e.setId(id);
        e.setName("Event " + id);
        e.setLocation("Tunis");
        e.setLatitude(36.8);
        e.setLongitude(10.18);
        e.setStartDate(LocalDateTime.now().plusDays(3));
        e.setEndDate(LocalDateTime.now().plusDays(4));
        e.setMaxCapacity(200);
        e.setCurrentParticipants(0);
        e.setStatus(EventStatus.ACCEPTED);
        e.setCategory(cat);
        e.setUserId(1L);
        e.setPrice(price);
        e.setEventType(type);
        return e;
    }

    private void stubEmptyBehavior(Long userId) {
        given(userBehaviorRepository.findPreferredEventTypes(userId))
                .willReturn(List.<Object[]>of(new Object[]{1L, 5L}));
        given(userBehaviorRepository.findPreferredLocation(userId))
                .willReturn(null);
        given(userBehaviorRepository.findPriceRange(userId))
                .willReturn(null);
        given(userBehaviorRepository.findPreferredEventTypes(userId))
                .willReturn(List.<Object[]>of(new Object[]{1L, 5L}));
    }

    @Test
    @DisplayName("getRecommendedEvents_shouldReturnPage_whenEventsExist")
    void getRecommendedEvents_shouldReturnPage_whenEventsExist() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        List<Event> events = List.of(
                buildEvent(1L, 1L, EventType.OUTDOOR, BigDecimal.TEN),
                buildEvent(2L, 2L, EventType.INDOOR, BigDecimal.ZERO)
        );

        given(eventRepository.findByStatus(EventStatus.ACCEPTED)).willReturn(events);
        stubEmptyBehavior(userId);

        Page<ScoredEventDTO> result = recommendationService.getRecommendedEvents(userId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("getRecommendedEvents_shouldReturnEmptyPage_whenNoEvents")
    void getRecommendedEvents_shouldReturnEmptyPage_whenNoEvents() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        given(eventRepository.findByStatus(EventStatus.ACCEPTED)).willReturn(Collections.emptyList());
        stubEmptyBehavior(userId);

        Page<ScoredEventDTO> result = recommendationService.getRecommendedEvents(userId, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("getRecommendedEvents_shouldReturnEmptyPage_whenOffsetExceedsTotal")
    void getRecommendedEvents_shouldReturnEmptyPage_whenOffsetExceedsTotal() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(5, 10); // offset = 50, but only 2 events

        List<Event> events = List.of(
                buildEvent(1L, 1L, EventType.OUTDOOR, BigDecimal.TEN)
        );

        given(eventRepository.findByStatus(EventStatus.ACCEPTED)).willReturn(events);
        stubEmptyBehavior(userId);

        Page<ScoredEventDTO> result = recommendationService.getRecommendedEvents(userId, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getRecommendedEvents_shouldScoreHigher_forMatchingCategoryAndType")
    void getRecommendedEvents_shouldScoreHigher_forMatchingCategoryAndType() {
        Long userId = 7L;
        Pageable pageable = PageRequest.of(0, 10);

        Event matchingEvent = buildEvent(1L, 5L, EventType.OUTDOOR, new BigDecimal("20.00"));
        Event nonMatchingEvent = buildEvent(2L, 9L, EventType.INDOOR, new BigDecimal("200.00"));

        given(eventRepository.findByStatus(EventStatus.ACCEPTED))
                .willReturn(List.of(nonMatchingEvent, matchingEvent));

        // User strongly prefers category 5
        given(userBehaviorRepository.findCategoryFrequency(userId))
                .willReturn(List.<Object[]>of(new Object[]{5L, 10L}));

        given(userBehaviorRepository.findPreferredLocation(userId))
                .willReturn(new Object[]{36.8, 10.18});

        given(userBehaviorRepository.findPriceRange(userId))
                .willReturn(new Object[]{new BigDecimal("0"), new BigDecimal("50")});

        given(userBehaviorRepository.findPreferredEventTypes(userId))
                .willReturn(List.<Object[]>of(new Object[]{1L}));

        Page<ScoredEventDTO> result = recommendationService.getRecommendedEvents(userId, pageable);

        // matchingEvent should score higher and appear first
        assertThat(result.getContent().get(0).getEvent().getId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getScore())
                .isGreaterThan(result.getContent().get(1).getScore());
    }

    @Test
    @DisplayName("getRecommendedEvents_shouldReturnCorrectPage_whenPaginated")
    void getRecommendedEvents_shouldReturnCorrectPage_whenPaginated() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 1); // Only 1 per page

        List<Event> events = List.of(
                buildEvent(1L, 1L, EventType.OUTDOOR, BigDecimal.TEN),
                buildEvent(2L, 2L, EventType.INDOOR, BigDecimal.ZERO)
        );

        given(eventRepository.findByStatus(EventStatus.ACCEPTED)).willReturn(events);
        stubEmptyBehavior(userId);

        Page<ScoredEventDTO> result = recommendationService.getRecommendedEvents(userId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }
}