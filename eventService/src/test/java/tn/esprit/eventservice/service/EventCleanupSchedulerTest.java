package tn.esprit.eventservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.scheduler.EventCleanupScheduler;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventCleanupSchedulerTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventCleanupScheduler scheduler;

    private Event pastEvent;
    private Event futureEvent;

    @BeforeEach
    void setUp() {
        pastEvent = new Event();
        pastEvent.setId(1L);
        pastEvent.setName("Past Event");
        pastEvent.setEndDate(LocalDateTime.now().minusDays(1));

        futureEvent = new Event();
        futureEvent.setId(2L);
        futureEvent.setName("Future Event");
        futureEvent.setEndDate(LocalDateTime.now().plusDays(1));
    }

    @Test
    void shouldDeleteFinishedEvents() {
        // Given
        when(eventRepository.findByEndDateBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(pastEvent));

        // When
        scheduler.deleteFinishedEvents();

        // Then
        verify(eventRepository, times(1)).findByEndDateBefore(any(LocalDateTime.class));
        verify(eventRepository, times(1)).deleteAll(List.of(pastEvent));
    }

    @Test
    void shouldDoNothingWhenNoFinishedEvents() {
        // Given
        when(eventRepository.findByEndDateBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        scheduler.deleteFinishedEvents();

        // Then
        verify(eventRepository, times(1)).findByEndDateBefore(any(LocalDateTime.class));
        verify(eventRepository, never()).deleteAll(any());
    }

    @Test
    void shouldDeleteMultipleFinishedEvents() {
        // Given
        Event pastEvent2 = new Event();
        pastEvent2.setId(3L);
        pastEvent2.setName("Past Event 2");
        pastEvent2.setEndDate(LocalDateTime.now().minusDays(5));

        when(eventRepository.findByEndDateBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(pastEvent, pastEvent2));

        // When
        scheduler.deleteFinishedEvents();

        // Then
        verify(eventRepository, times(1)).deleteAll(List.of(pastEvent, pastEvent2));
    }

    @Test
    void shouldNotDeleteFutureEvents() {
        // Given
        when(eventRepository.findByEndDateBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        scheduler.deleteFinishedEvents();

        // Then
        verify(eventRepository, never()).deleteAll(List.of(futureEvent));
    }
}
