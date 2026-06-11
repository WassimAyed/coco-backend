package tn.esprit.serviceetudiant.dto;

import jakarta.validation.constraints.NotNull;
import tn.esprit.serviceetudiant.enums.ServiceModerationStatus;

public record ServiceModerationUpdateRequest(
        @NotNull ServiceModerationStatus status
) {
}