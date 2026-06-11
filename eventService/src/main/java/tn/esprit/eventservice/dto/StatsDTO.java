package tn.esprit.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsDTO {

    private long totalEvents;
    private long totalParticipants;
    private long totalCategories;
    private long availableEvents;
    private Map<String, Long> eventsByStatus;
    private Map<String, Long> eventsByCategory;
    private Map<String, Long> participantsByEvent;
}