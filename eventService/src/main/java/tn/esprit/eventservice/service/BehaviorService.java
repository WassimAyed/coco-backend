package tn.esprit.eventservice.service;

import org.springframework.stereotype.Service;
import tn.esprit.eventservice.dto.BehaviorDTO;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.UserBehavior;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.repository.UserBehaviorRepository;

import java.util.Set;

@Service
public class BehaviorService {

    private static final Set<String> ALLOWED_ACTIONS = Set.of(
            "VIEW", "PARTICIPATE", "BOOKMARK", "COMMENT",
            "LIKE", "LOVE", "HAHA", "WOW", "SAD", "ANGRY",
            "RATE_1", "RATE_2", "RATE_3", "RATE_4", "RATE_5"
    );

    private final UserBehaviorRepository userBehaviorRepository;
    private final EventRepository eventRepository;

    public BehaviorService(UserBehaviorRepository userBehaviorRepository, EventRepository eventRepository) {
        this.userBehaviorRepository = userBehaviorRepository;
        this.eventRepository = eventRepository;
    }

    public void save(BehaviorDTO dto) {
        if (dto.getUserId() == null || dto.getEventId() == null || dto.getActionType() == null) {
            throw new IllegalArgumentException("userId, eventId and actionType are required");
        }

        String actionType = dto.getActionType().trim().toUpperCase();
        if (!ALLOWED_ACTIONS.contains(actionType)) {
            throw new IllegalArgumentException("Unsupported actionType: " + dto.getActionType());
        }

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Event introuvable : " + dto.getEventId()));

        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(dto.getUserId());
        behavior.setEventId(dto.getEventId());
        
        Long categoryId = dto.getCategoryId();
        if (categoryId == null && event.getCategory() != null) {
            categoryId = event.getCategory().getId();
        }
        behavior.setCategoryId(categoryId);
        
        behavior.setActionType(actionType);
        behavior.setLastLat(dto.getLat());
        behavior.setLastLng(dto.getLng());

        userBehaviorRepository.save(behavior);
    }
}
