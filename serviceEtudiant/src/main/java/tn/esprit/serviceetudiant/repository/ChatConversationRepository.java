package tn.esprit.serviceetudiant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.serviceetudiant.entity.ChatConversation;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    Optional<ChatConversation> findByRequestId(Long requestId);
    List<ChatConversation> findByRequesterIdOrProviderIdOrderByUpdatedAtDesc(Long requesterId, Long providerId);
    List<ChatConversation> findByServiceId(Long serviceId);
    void deleteByServiceId(Long serviceId);
}