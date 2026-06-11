package tn.esprit.serviceetudiant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.serviceetudiant.entity.ChatMessage;

import java.util.Collection;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversationIdOrderBySentAtAsc(Long conversationId);
    ChatMessage findTopByConversationIdOrderBySentAtDesc(Long conversationId);
    void deleteByConversationId(Long conversationId);
    void deleteByConversationIdIn(Collection<Long> conversationIds);
}