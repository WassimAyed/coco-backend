package tn.esprit.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoredEventDTO {

    private EventDTO event;
    private double score;
    private String scoreLabel;

    public ScoredEventDTO(EventDTO event, double score) {
        this.event = event;
        this.score = score;
        this.scoreLabel = resolveScoreLabel(score);
    }

    private String resolveScoreLabel(double value) {
        if (value > 0.8) {
            return "Top pick";
        }
        if (value > 0.6) {
            return "Recommande";
        }
        return "Peut vous interesser";
    }
}
