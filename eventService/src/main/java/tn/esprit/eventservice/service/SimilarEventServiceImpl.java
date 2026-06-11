package tn.esprit.eventservice.service;

import org.springframework.stereotype.Service;
import tn.esprit.eventservice.dto.SimilarEventDTO;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.EventRatingRepository;
import tn.esprit.eventservice.repository.EventRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SimilarEventServiceImpl {

    private final EventRepository eventRepository;
    private final EventRatingRepository ratingRepository;

    public SimilarEventServiceImpl(EventRepository eventRepository,
                                   EventRatingRepository ratingRepository) {
        this.eventRepository = eventRepository;
        this.ratingRepository = ratingRepository;
    }

    // ✅ Méthode principale
    public List<SimilarEventDTO> getSimilarEvents(Long eventId, int limit) {

        Event source = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable : " + eventId));

        LocalDateTime dateFrom = source.getStartDate().minusDays(30);
        LocalDateTime dateTo   = source.getStartDate().plusDays(30);
        Long categoryId = source.getCategory() != null ? source.getCategory().getId() : -1L;

        List<Event> candidates = eventRepository.findSimilarEvents(
                eventId,
                categoryId,
                source.getLocation(),
                dateFrom,
                dateTo
        );

        return candidates.stream()
                .sorted(Comparator.comparingInt(e -> -similarityScore(e, source)))
                .limit(limit)
                .map(e -> toDTO(e, source))
            .toList();
    }

    // ✅ Formule Haversine — distance en km entre deux points GPS
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // ✅ Score de similarité — plus le score est élevé, plus l'event est similaire
    private int similarityScore(Event e, Event source) {
        int score = 0;

        // +2 si même catégorie
        if (e.getCategory() != null && source.getCategory() != null
                && e.getCategory().getId().equals(source.getCategory().getId()))
            score += 2;

        // +2 si distance < 10 km / +1 si distance < 50 km
        if (e.getLatitude() != null && e.getLongitude() != null
                && source.getLatitude() != null && source.getLongitude() != null) {
            double distance = haversineKm(
                    source.getLatitude(), source.getLongitude(),
                    e.getLatitude(), e.getLongitude()
            );
            if (distance < 10)      score += 2;
            else if (distance < 50) score += 1;
        }

        // +1 si date proche ± 30 jours
        long daysDiff = Math.abs(ChronoUnit.DAYS.between(
                e.getStartDate(), source.getStartDate()));
        if (daysDiff <= 30) score += 1;

        return score;
    }

    // ✅ Raison de similarité affichée sur le frontend
    private String buildSimilarityReason(Event e, Event source) {
        List<String> reasons = new ArrayList<>();

        if (e.getCategory() != null && source.getCategory() != null
                && e.getCategory().getId().equals(source.getCategory().getId()))
            reasons.add("Même catégorie");

        if (e.getLatitude() != null && e.getLongitude() != null
                && source.getLatitude() != null && source.getLongitude() != null) {
            double distance = haversineKm(
                    source.getLatitude(), source.getLongitude(),
                    e.getLatitude(), e.getLongitude()
            );
            if (distance < 10)
                reasons.add("Même zone");
            else if (distance < 50)
                reasons.add("À " + (int) distance + " km");
        }

        long daysDiff = Math.abs(ChronoUnit.DAYS.between(
                e.getStartDate(), source.getStartDate()));
        if (daysDiff <= 30)
            reasons.add("Période proche");

        return reasons.isEmpty() ? "Event similaire" : String.join(" • ", reasons);
    }

    // ✅ Conversion Event → SimilarEventDTO
    private SimilarEventDTO toDTO(Event e, Event source) {
        SimilarEventDTO dto = new SimilarEventDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setLocation(e.getLocation());
        dto.setImageUrl(e.getImageUrl());
        dto.setStartDate(e.getStartDate());
        dto.setMaxCapacity(e.getMaxCapacity());
        dto.setCurrentParticipants(e.getCurrentParticipants());

        if (e.getCategory() != null)
            dto.setCategoryName(e.getCategory().getName());

        Double avg = ratingRepository.findAverageRatingByEventId(e.getId());
        dto.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);

        dto.setSimilarityReason(buildSimilarityReason(e, source));
        return dto;
    }
}