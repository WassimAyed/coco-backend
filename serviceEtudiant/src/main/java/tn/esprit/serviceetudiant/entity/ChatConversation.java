package tn.esprit.serviceetudiant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.serviceetudiant.enums.ServiceRequestStatus;

import java.time.Instant;

@Entity
@Table(name = "chat_conversations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long requestId;

    @Column(nullable = false)
    private Long serviceId;

    @Column(nullable = false, length = 160)
    private String serviceTitle;

    @Column(nullable = false)
    private Long requesterId;

    @Column(nullable = false, length = 120)
    private String requesterName;

    @Column(nullable = false)
    private Long providerId;

    @Column(nullable = false, length = 120)
    private String providerName;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}