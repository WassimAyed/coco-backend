package tn.esprit.eventservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tn.esprit.eventservice.dto.EventDTO;
import tn.esprit.eventservice.dto.ScoredEventDTO;
import tn.esprit.eventservice.dto.UserHistorique;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventStatus;
import tn.esprit.eventservice.entity.EventType;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.repository.UserBehaviorRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    private final EventRepository eventRepository;
    private final UserBehaviorRepository userBehaviorRepository;

    public RecommendationService(EventRepository eventRepository, UserBehaviorRepository userBehaviorRepository) {
        this.eventRepository = eventRepository;
        this.userBehaviorRepository = userBehaviorRepository;
    }

    public Page<ScoredEventDTO> getRecommendedEvents(Long userId, Pageable pageable) {
        UserHistorique profile = buildUserHistorique(userId);
        List<ScoredEventDTO> scoredEvents = eventRepository.findByStatus(EventStatus.ACCEPTED).stream()
                .map(event -> new ScoredEventDTO(mapToDTO(event), computeScore(event, profile)))
                .sorted(Comparator.comparingDouble(ScoredEventDTO::getScore).reversed())
            .toList();

        int start = (int) pageable.getOffset();
        if (start >= scoredEvents.size()) {
            return new PageImpl<>(List.of(), pageable, scoredEvents.size());
        }

        int end = Math.min(start + pageable.getPageSize(), scoredEvents.size());
        return new PageImpl<>(scoredEvents.subList(start, end), pageable, scoredEvents.size());
    }

    private UserHistorique buildUserHistorique(Long userId) {
        UserHistorique profile = new UserHistorique();
        profile.setUserId(userId);

        Map<Long, Long> categoryFrequency = new HashMap<>();
        for (Object[] row : userBehaviorRepository.findCategoryFrequency(userId)) {
            Long categoryId = (Long) row[0];
            Long frequency = ((Number) row[1]).longValue();
            categoryFrequency.put(categoryId, frequency);
        }
        profile.setCategoryFrequency(categoryFrequency);

        Object[] location = userBehaviorRepository.findPreferredLocation(userId);
        if (location != null && location.length == 2 && location[0] != null && location[1] != null) {
            profile.setPreferredLat(((Number) location[0]).doubleValue());
            profile.setPreferredLng(((Number) location[1]).doubleValue());
        }

        Object[] priceRange = userBehaviorRepository.findPriceRange(userId);
        if (priceRange != null && priceRange.length == 2 && priceRange[0] != null && priceRange[1] != null) {
            profile.setMinPrice((BigDecimal) priceRange[0]);
            profile.setMaxPrice((BigDecimal) priceRange[1]);
        }

        List<Object[]> preferredTypes = userBehaviorRepository.findPreferredEventTypes(userId);
        if (!preferredTypes.isEmpty() && preferredTypes.get(0)[0] != null) {
            profile.setPreferredEventType(String.valueOf(preferredTypes.get(0)[0]));
        }

        return profile;
    }

    private double computeScore(Event event, UserHistorique profile) {
        return (computeCategoryScore(event, profile) * 0.35)
                + (computeGeoScore(event, profile) * 0.30)
                + (computePriceScore(event, profile) * 0.20)
                + (computeTypeScore(event, profile) * 0.15);
    }

    private double computeCategoryScore(Event event, UserHistorique profile) {
        if (event.getCategory() == null || profile.getCategoryFrequency() == null || profile.getCategoryFrequency().isEmpty()) {
            return 0.0;
        }

        Long eventCategoryId = event.getCategory().getId();
        long frequency = profile.getCategoryFrequency().getOrDefault(eventCategoryId, 0L);
        long maxFrequency = profile.getCategoryFrequency().values().stream().mapToLong(Long::longValue).max().orElse(1L);
        return maxFrequency == 0 ? 0.0 : (double) frequency / (double) maxFrequency;
    }

    private double computeGeoScore(Event event, UserHistorique profile) {
        if (profile.getPreferredLat() == null || profile.getPreferredLng() == null
                || event.getLatitude() == null || event.getLongitude() == null) {
            return 0.0;
        }

        double distanceKm = haversine(
                profile.getPreferredLat(),
                profile.getPreferredLng(),
                event.getLatitude(),
                event.getLongitude());

        return 1.0 / (1.0 + distanceKm);
    }

    private double computePriceScore(Event event, UserHistorique profile) {
        if (event.getPrice() == null || profile.getMinPrice() == null || profile.getMaxPrice() == null) {
            return 0.0;
        }

        return event.getPrice().compareTo(profile.getMinPrice()) >= 0
                && event.getPrice().compareTo(profile.getMaxPrice()) <= 0 ? 1.0 : 0.0;
    }

    private double computeTypeScore(Event event, UserHistorique profile) {
        if (profile.getPreferredEventType() == null || event.getEventType() == null) {
            return 0.3;
        }

        return profile.getPreferredEventType().equalsIgnoreCase(event.getEventType().name()) ? 1.0 : 0.3;
    }

    private EventDTO mapToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setLocation(event.getLocation());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setDescription(event.getDescription());
        dto.setImageUrl(event.getImageUrl());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setMaxCapacity(event.getMaxCapacity());
        dto.setCurrentParticipants(event.getCurrentParticipants());
        dto.setUserId(event.getUserId());
        dto.setStatus(event.getStatus());
        dto.setEventType(event.getEventType() != null ? event.getEventType() : EventType.OUTDOOR);
        dto.setPrice(event.getPrice());
        dto.setTemperature(event.getTemperature());
        dto.setPrecipitationMm(event.getPrecipitationMm());
        dto.setWindSpeedKmh(event.getWindSpeedKmh());
        dto.setWeatherCode(event.getWeatherCode());
        dto.setWeatherLabel(event.getWeatherLabel());
        dto.setPredictedParticipants(event.getPredictedParticipants());
        if (event.getCategory() != null) {
            dto.setCategoryId(event.getCategory().getId());
        }

        if (dto.getStartDate() == null) {
            dto.setStartDate(LocalDateTime.now().plusDays(1));
        }
        if (dto.getEndDate() == null) {
            dto.setEndDate(dto.getStartDate().plusHours(1));
        }
        return dto;
    }

    // Distance en km entre deux points GPS.
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int r = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
