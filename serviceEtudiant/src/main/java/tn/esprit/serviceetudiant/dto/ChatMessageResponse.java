package tn.esprit.serviceetudiant.dto;

import java.time.Instant;

public record ChatMessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderName,
        String content,
        String imageUrl,
        Instant sentAt,
        Instant editedAt,
        Instant deletedAt
) {
}
