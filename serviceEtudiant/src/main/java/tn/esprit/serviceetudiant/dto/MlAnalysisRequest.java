package tn.esprit.serviceetudiant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * Payload sent to the ML moderation service (POST /analyze).
 * Field names match the Python Pydantic schema in machine_learning/models/schemas.py.
 */
@Builder
public record MlAnalysisRequest(
        String title,

        @JsonProperty("short_description")
        String shortDescription,

        double price,
        String category,

        @JsonProperty("image_url")
        String imageUrl,

        @JsonProperty("delivery_mode")
        String deliveryMode,

        List<String> tags
) {}
