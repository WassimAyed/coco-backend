package tn.esprit.serviceetudiant.dto;

import jakarta.validation.constraints.NotNull;
import tn.esprit.serviceetudiant.enums.ServiceRequestStatus;

public record ServiceRequestStatusUpdateRequest(
        @NotNull ServiceRequestStatus status
) {
}