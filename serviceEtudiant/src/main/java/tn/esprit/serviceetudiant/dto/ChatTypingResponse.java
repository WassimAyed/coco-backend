package tn.esprit.serviceetudiant.dto;

import java.time.Instant;

public record ChatTypingResponse(
        Long conversationId,
        Long senderId,
        String senderName,
        boolean typing,
        Instant sentAt
) {
}
