package tn.esprit.eventservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventservice.dto.EventRatingDTO;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventRating;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.EventRatingRepository;
import tn.esprit.eventservice.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventRatingServiceImplTest {

    @Mock EventRatingRepository ratingRepository;
    @Mock EventRepository eventRepository;
    @InjectMocks EventRatingServiceImpl ratingService;

    private Event event;
    private EventRating rating;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1L);
        event.setName("Tech Summit");

        rating = new EventRating();
        rating.setId(1L);
        rating.setEvent(event);
        rating.setUserId(5L);
        rating.setRating(4);
        rating.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldRateEventForFirstTime() {
        EventRatingDTO dto = new EventRatingDTO();
        dto.setEventId(1L);
        dto.setUserId(5L);
        dto.setRating(4);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ratingRepository.findByEventIdAndUserId(1L, 5L)).thenReturn(Optional.empty());
        when(ratingRepository.save(any())).thenReturn(rating);
        when(ratingRepository.findAverageRatingByEventId(1L)).thenReturn(4.0);
        when(ratingRepository.countByEventId(1L)).thenReturn(1L);

        EventRatingDTO result = ratingService.rateEvent(dto);

        assertNotNull(result);
        assertEquals(4, result.getRating());
        assertEquals(4.0, result.getAverageRating());
        verify(ratingRepository).save(any());
    }

    @Test
    void shouldUpdateExistingRating() {
        EventRatingDTO dto = new EventRatingDTO();
        dto.setEventId(1L);
        dto.setUserId(5L);
        dto.setRating(5);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ratingRepository.findByEventIdAndUserId(1L, 5L)).thenReturn(Optional.of(rating));
        when(ratingRepository.save(any())).thenReturn(rating);
        when(ratingRepository.findAverageRatingByEventId(1L)).thenReturn(5.0);
        when(ratingRepository.countByEventId(1L)).thenReturn(1L);

        EventRatingDTO result = ratingService.rateEvent(dto);

        assertNotNull(result);
        verify(ratingRepository).save(any());
    }

    @Test
    void shouldThrowWhenRatingNonExistentEvent() {
        EventRatingDTO dto = new EventRatingDTO();
        dto.setEventId(99L);
        dto.setUserId(5L);

        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ratingService.rateEvent(dto));
    }

    @Test
    void shouldGetEventRatingStats() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ratingRepository.findAverageRatingByEventId(1L)).thenReturn(4.5);
        when(ratingRepository.countByEventId(1L)).thenReturn(10L);

        EventRatingDTO result = ratingService.getEventRatingStats(1L);

        assertEquals(1L, result.getEventId());
        assertEquals(4.5, result.getAverageRating());
        assertEquals(10L, result.getTotalRatings());
    }

    @Test
    void shouldGetUserRating() {
        when(ratingRepository.findByEventIdAndUserId(1L, 5L)).thenReturn(Optional.of(rating));

        EventRatingDTO result = ratingService.getUserRating(1L, 5L);

        assertNotNull(result);
        assertEquals(4, result.getRating());
    }

    @Test
    void shouldThrowWhenUserRatingNotFound() {
        when(ratingRepository.findByEventIdAndUserId(1L, 99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ratingService.getUserRating(1L, 99L));
    }

    @Test
    void shouldDeleteRating() {
        when(ratingRepository.findByEventIdAndUserId(1L, 5L)).thenReturn(Optional.of(rating));

        ratingService.deleteRating(1L, 5L);

        verify(ratingRepository).delete(rating);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentRating() {
        when(ratingRepository.findByEventIdAndUserId(1L, 99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ratingService.deleteRating(1L, 99L));
    }
}