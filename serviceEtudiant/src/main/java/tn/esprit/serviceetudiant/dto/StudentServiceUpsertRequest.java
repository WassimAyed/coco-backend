package tn.esprit.serviceetudiant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tn.esprit.serviceetudiant.enums.DeliveryMode;
import tn.esprit.serviceetudiant.enums.ServiceCategory;

import java.util.List;

public record StudentServiceUpsertRequest(
        @NotBlank @Size(min = 8, max = 160) String title,
        @NotBlank @Size(min = 20, max = 280) String shortDescription,
        @NotNull ServiceCategory category,
        @NotBlank String priceLabel,
        @NotNull DeliveryMode deliveryMode,
        @NotNull List<String> tags,
        @NotBlank String location,
        String coverImageUrl,
        Long providerId,
        String providerName,
        String providerHeadline,
        String providerAvatar,
        String providerDepartment,
        Boolean featured
) {
}
