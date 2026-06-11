package tn.esprit.serviceetudiant.dto;

import tn.esprit.serviceetudiant.enums.ServiceCategory;
import tn.esprit.serviceetudiant.enums.ServiceRequestStatus;

import java.time.Instant;

public record ServiceRequestResponse(
        Long id,
        Long serviceId,
        String serviceTitle,
        ServiceCategory serviceCategory,
        Long requesterId,
        String requesterName,
        String requesterDepartment,
        String requesterAvatar,
        Long providerId,
        String providerName,
        String message,
        String preferredDate,
        ServiceRequestStatus status,
        String budgetLabel,
        Instant createdAt
) {
}