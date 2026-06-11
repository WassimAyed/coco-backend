package tn.esprit.lostfoundservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.lostfoundservice.entity.ClaimStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemClaimResponseDTO {

    private Long id;
    private Long itemId;
    private Long claimantUserId;
    private String proofMessage;
    private ClaimStatus status;
    private String ownerDecisionComment;
    private Long decidedByUserId;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
