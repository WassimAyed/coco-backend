package tn.esprit.serviceetudiant.dto;

import java.time.Instant;

public record ConversationResponse(
        Long id,
        Long requestId,
        Long serviceId,
        String serviceTitle,
        Long requesterId,
        String requesterName,
        Long providerId,
        String providerName,
        Long otherParticipantId,
        String otherParticipantName,
        boolean active,
        String lastMessage,
        Instant lastMessageAt,
        Instant createdAt,
        Instant updatedAt
) {
}