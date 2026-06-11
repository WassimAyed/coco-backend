package tn.esprit.eventservice.service;

import org.springframework.stereotype.Service;
import tn.esprit.eventservice.dto.StatsDTO;
import tn.esprit.eventservice.repository.CategoryRepository;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.repository.ParticipantRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsServiceImpl implements IStatsService {

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final CategoryRepository categoryRepository;

    public StatsServiceImpl(EventRepository eventRepository,
                            ParticipantRepository participantRepository,
                            CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public StatsDTO getGlobalStats() {
        StatsDTO stats = new StatsDTO();

        // Totaux
        stats.setTotalEvents(eventRepository.count());
        stats.setTotalParticipants(participantRepository.count());
        stats.setTotalCategories(categoryRepository.count());
        stats.setAvailableEvents(eventRepository.countAvailableEvents());

        // Events par statut
        Map<String, Long> byStatus = new LinkedHashMap<>();
        List<Object[]> statusResults = eventRepository.countByStatus();
        for (Object[] row : statusResults) {
            byStatus.put(asLabel(row[0], "UNKNOWN"), asLong(row[1]));
        }
        stats.setEventsByStatus(byStatus);

        // Events par catégorie
        Map<String, Long> byCategory = new LinkedHashMap<>();
        List<Object[]> categoryResults = eventRepository.countByCategory();
        for (Object[] row : categoryResults) {
            byCategory.put(asLabel(row[0], "Sans catégorie"), asLong(row[1]));
        }
        stats.setEventsByCategory(byCategory);

        // Participants par event
        Map<String, Long> byEvent = new LinkedHashMap<>();
        List<Object[]> eventResults = participantRepository.countByEvent();
        for (Object[] row : eventResults) {
            byEvent.put(asLabel(row[0], "Événement sans nom"), asLong(row[1]));
        }
        stats.setParticipantsByEvent(byEvent);

        return stats;
    }

    private String asLabel(Object value, String fallback) {
        return value != null ? value.toString() : fallback;
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }
}