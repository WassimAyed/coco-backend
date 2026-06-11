package tn.esprit.serviceetudiant.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ServiceTagsUpdateRequest(
        @NotNull List<String> tags
) {
}
