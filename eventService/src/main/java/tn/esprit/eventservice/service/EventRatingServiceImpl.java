package tn.esprit.eventservice.service;

import org.springframework.stereotype.Service;
import tn.esprit.eventservice.dto.EventRatingDTO;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventRating;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.EventRatingRepository;
import tn.esprit.eventservice.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EventRatingServiceImpl {

    private final EventRatingRepository ratingRepository;
    private final EventRepository eventRepository;

    public EventRatingServiceImpl(EventRatingRepository ratingRepository,
                                  EventRepository eventRepository) {
        this.ratingRepository = ratingRepository;
        this.eventRepository = eventRepository;
    }

    // ➕ Ajouter ou modifier un rating
    public EventRatingDTO rateEvent(EventRatingDTO dto) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable : " + dto.getEventId()));

        EventRating rating;

        // Si l'user a déjà noté → on met à jour
        Optional<EventRating> existing = ratingRepository.findByEventIdAndUserId(dto.getEventId(), dto.getUserId());

        if (existing.isPresent()) {
            rating = existing.get();
            rating.setRating(dto.getRating()); // mise à jour
        } else {
            // Nouveau rating
            rating = new EventRating();
            rating.setEvent(event);
            rating.setUserId(dto.getUserId());
            rating.setRating(dto.getRating());
            rating.setCreatedAt(LocalDateTime.now());
        }

        EventRating saved = ratingRepository.save(rating);

        // Construire la réponse avec moyenne et total
        EventRatingDTO response = toDTO(saved);
        response.setAverageRating(ratingRepository.findAverageRatingByEventId(dto.getEventId()));
        response.setTotalRatings(ratingRepository.countByEventId(dto.getEventId()));

        return response;
    }

    // 📊 Récupérer la moyenne d'un event
    public EventRatingDTO getEventRatingStats(Long eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable : " + eventId));

        EventRatingDTO dto = new EventRatingDTO();
        dto.setEventId(eventId);

        Double avg = ratingRepository.findAverageRatingByEventId(eventId);
        Long total = ratingRepository.countByEventId(eventId);

        dto.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0); // arrondi 1 décimale
        dto.setTotalRatings(total);

        return dto;
    }

    // 🔍 Récupérer le rating d'un user pour un event
    public EventRatingDTO getUserRating(Long eventId, Long userId) {
        EventRating rating = ratingRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun rating trouvé"));
        return toDTO(rating);
    }

    // ❌ Supprimer un rating
    public void deleteRating(Long eventId, Long userId) {
        EventRating rating = ratingRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun rating trouvé"));
        ratingRepository.delete(rating);
    }

    private EventRatingDTO toDTO(EventRating r) {
        EventRatingDTO dto = new EventRatingDTO();
        dto.setId(r.getId());
        dto.setEventId(r.getEvent().getId());
        dto.setUserId(r.getUserId());
        dto.setRating(r.getRating());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}