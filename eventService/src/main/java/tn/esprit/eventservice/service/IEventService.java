package tn.esprit.eventservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.eventservice.dto.EventDTO;
import tn.esprit.eventservice.entity.EventStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface IEventService {
    EventDTO createEvent(EventDTO dto);
    EventDTO updateEvent(Long id, EventDTO dto);
    void deleteEvent(Long id);
    EventDTO getEventById(Long id);
    List<EventDTO> getAllEvents();
    List<EventDTO> getAllEvents(Long userId);
    Page<EventDTO> getAllEvents(Pageable pageable);
    Page<EventDTO> getAllEvents(Long userId, Pageable pageable);
    Page<EventDTO> getEventsCreatedByUser(Long userId, Pageable pageable);
    Page<EventDTO> getParticipatedEvents(String email, String phone, Pageable pageable);
    List<EventDTO> getEventsByStatus(EventStatus status);
    Page<EventDTO> getEventsByStatus(EventStatus status, Pageable pageable);
    List<EventDTO> getEventsByCategory(Long categoryId);
    Page<EventDTO> getEventsByCategory(Long categoryId, Pageable pageable);
    List<EventDTO> searchEventsByName(String name);
    Page<EventDTO> searchEventsByName(String name, Pageable pageable);
    List<EventDTO> getAvailableEvents();
    Page<EventDTO> getAvailableEvents(Pageable pageable);
    List<EventDTO> getEventsByDateRange(LocalDateTime from, LocalDateTime to);
    Page<EventDTO> getEventsByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable);
    List<EventDTO> getEventsNearby(Double lat, Double lng, Double radiusKm);
    Page<EventDTO> getEventsNearby(Double lat, Double lng, Double radiusKm, Pageable pageable);
    EventDTO updateStatus(Long id, EventStatus status);
}