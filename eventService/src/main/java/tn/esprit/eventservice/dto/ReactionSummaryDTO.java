package tn.esprit.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.eventservice.entity.ReactionType;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionSummaryDTO {

    private Long eventId;
    private long totalReactions;
    private Map<ReactionType, Long> reactionCounts;
}