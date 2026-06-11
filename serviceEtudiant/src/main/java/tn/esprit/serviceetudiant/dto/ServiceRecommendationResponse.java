package tn.esprit.serviceetudiant.dto;

import java.util.List;

public record ServiceRecommendationResponse(
        Long serviceId,
        String title,
        String reason,
        int score,
        List<String> matchingTags
) {
}