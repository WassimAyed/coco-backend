package tn.esprit.lostfoundservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "item_claim", indexes = {
        @Index(name = "idx_item_claim_item_id", columnList = "itemId"),
        @Index(name = "idx_item_claim_claimant_id", columnList = "claimantUserId"),
        @Index(name = "idx_item_claim_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Long claimantUserId;

    @Column(nullable = false, length = 1000)
    private String proofMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimStatus status;

    @Column(length = 1000)
    private String ownerDecisionComment;

    private Long decidedByUserId;

    private LocalDateTime decidedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
