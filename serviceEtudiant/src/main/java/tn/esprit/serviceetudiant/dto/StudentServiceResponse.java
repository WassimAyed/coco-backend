package tn.esprit.serviceetudiant.dto;

import tn.esprit.serviceetudiant.enums.DeliveryMode;
import tn.esprit.serviceetudiant.enums.ServiceCategory;
import tn.esprit.serviceetudiant.enums.ServiceModerationStatus;

import java.time.Instant;
import java.util.List;

public record StudentServiceResponse(
        Long id,
        String title,
        String slug,
        String shortDescription,
        ServiceCategory category,
        String priceLabel,
        DeliveryMode deliveryMode,
        List<String> tags,
        String location,
        Long providerId,
        String providerName,
        String providerHeadline,
        String providerAvatar,
        String providerDepartment,
        String coverImageUrl,
        boolean featured,
        int requestCount,
        ServiceModerationStatus moderationStatus,
        Instant moderatedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
