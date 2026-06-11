package tn.esprit.serviceetudiant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatTypingRequest(
        @NotNull Long conversationId,
        @NotNull Long senderId,
        @NotBlank String senderName,
        @NotNull Boolean typing
) {
}
