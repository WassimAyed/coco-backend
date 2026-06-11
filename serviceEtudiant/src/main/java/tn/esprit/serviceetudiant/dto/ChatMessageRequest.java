package tn.esprit.serviceetudiant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotNull Long conversationId,
        @NotNull Long senderId,
        @NotBlank String senderName,
        @NotBlank @Size(min = 1, max = 4000) String content
) {
}