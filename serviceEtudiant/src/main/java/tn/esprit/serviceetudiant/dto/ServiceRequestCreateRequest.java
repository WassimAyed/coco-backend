package tn.esprit.serviceetudiant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceRequestCreateRequest(
        @NotNull Long requesterId,
        @NotBlank String requesterName,
        String requesterDepartment,
        String requesterAvatar,
        @NotBlank @Size(min = 20, max = 2500) String message,
        @NotBlank String preferredDate,
        @NotBlank String budgetLabel
) {
}
