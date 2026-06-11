package tn.esprit.lostfoundservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.lostfoundservice.DTO.ItemClaimDecisionDTO;
import tn.esprit.lostfoundservice.DTO.ItemClaimRequestDTO;
import tn.esprit.lostfoundservice.DTO.ItemClaimResponseDTO;
import tn.esprit.lostfoundservice.entity.ClaimStatus;
import tn.esprit.lostfoundservice.entity.ItemClaim;
import tn.esprit.lostfoundservice.entity.LostItem;
import tn.esprit.lostfoundservice.entity.LostItemStatus;
import tn.esprit.lostfoundservice.exception.ItemNotFoundException;
import tn.esprit.lostfoundservice.exception.UnauthorizedAccessException;
import tn.esprit.lostfoundservice.repository.ItemClaimRepository;
import tn.esprit.lostfoundservice.repository.LostItemRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemClaimService {

    private final ItemClaimRepository claimRepository;
    private final LostItemRepository lostItemRepository;

    @Transactional
    public ItemClaimResponseDTO createClaim(Long itemId, Long claimantUserId, ItemClaimRequestDTO dto) {
        LostItem item = lostItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + itemId));

        if (item.getUserId().equals(claimantUserId)) {
            throw new UnauthorizedAccessException("You cannot claim your own item");
        }

        if (item.getStatus() != LostItemStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE items can be claimed");
        }

        boolean alreadyPending = claimRepository.existsByItemIdAndClaimantUserIdAndStatus(
                itemId, claimantUserId, ClaimStatus.PENDING
        );
        if (alreadyPending) {
            throw new IllegalStateException("You already have a pending claim for this item");
        }

        ItemClaim claim = ItemClaim.builder()
                .itemId(itemId)
                .claimantUserId(claimantUserId)
                .proofMessage(dto.getProofMessage())
                .status(ClaimStatus.PENDING)
                .build();

        return map(claimRepository.save(claim));
    }

    public List<ItemClaimResponseDTO> getMyClaims(Long claimantUserId) {
        return claimRepository.findByClaimantUserIdOrderByCreatedAtDesc(claimantUserId)
                .stream()
                .map(this::map)
                .toList();
    }

    public List<ItemClaimResponseDTO> getClaimsForMyItem(Long itemId, Long ownerUserId) {
        LostItem item = lostItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + itemId));

        if (!item.getUserId().equals(ownerUserId)) {
            throw new UnauthorizedAccessException("You can only view claims for your own item");
        }

        return claimRepository.findByItemIdOrderByCreatedAtDesc(itemId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional
    public ItemClaimResponseDTO approveClaim(Long claimId, Long ownerUserId, ItemClaimDecisionDTO decisionDTO) {
        ItemClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ItemNotFoundException("Claim not found with id: " + claimId));

        LostItem item = lostItemRepository.findById(claim.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + claim.getItemId()));

        if (!item.getUserId().equals(ownerUserId)) {
            throw new UnauthorizedAccessException("Only item owner can approve claims");
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalStateException("Only pending claims can be approved");
        }

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setOwnerDecisionComment(decisionDTO.getComment());
        claim.setDecidedByUserId(ownerUserId);
        claim.setDecidedAt(LocalDateTime.now());

        item.setStatus(LostItemStatus.RESOLVED);
        lostItemRepository.save(item);

        claimRepository.findByItemIdOrderByCreatedAtDesc(item.getId()).stream()
                .filter(other -> !other.getId().equals(claim.getId()) && other.getStatus() == ClaimStatus.PENDING)
                .forEach(other -> {
                    other.setStatus(ClaimStatus.REJECTED);
                    other.setOwnerDecisionComment("Another claim was approved");
                    other.setDecidedByUserId(ownerUserId);
                    other.setDecidedAt(LocalDateTime.now());
                    claimRepository.save(other);
                });

        return map(claimRepository.save(claim));
    }

    @Transactional
    public ItemClaimResponseDTO rejectClaim(Long claimId, Long ownerUserId, ItemClaimDecisionDTO decisionDTO) {
        ItemClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ItemNotFoundException("Claim not found with id: " + claimId));

        LostItem item = lostItemRepository.findById(claim.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + claim.getItemId()));

        if (!item.getUserId().equals(ownerUserId)) {
            throw new UnauthorizedAccessException("Only item owner can reject claims");
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalStateException("Only pending claims can be rejected");
        }

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setOwnerDecisionComment(decisionDTO.getComment());
        claim.setDecidedByUserId(ownerUserId);
        claim.setDecidedAt(LocalDateTime.now());

        return map(claimRepository.save(claim));
    }

    @Transactional
    public ItemClaimResponseDTO cancelMyClaim(Long claimId, Long claimantUserId) {
        ItemClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ItemNotFoundException("Claim not found with id: " + claimId));

        if (!claim.getClaimantUserId().equals(claimantUserId)) {
            throw new UnauthorizedAccessException("You can only cancel your own claim");
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalStateException("Only pending claims can be canceled");
        }

        claim.setStatus(ClaimStatus.CANCELED);
        claim.setDecidedByUserId(claimantUserId);
        claim.setDecidedAt(LocalDateTime.now());

        return map(claimRepository.save(claim));
    }

    private ItemClaimResponseDTO map(ItemClaim claim) {
        return ItemClaimResponseDTO.builder()
                .id(claim.getId())
                .itemId(claim.getItemId())
                .claimantUserId(claim.getClaimantUserId())
                .proofMessage(claim.getProofMessage())
                .status(claim.getStatus())
                .ownerDecisionComment(claim.getOwnerDecisionComment())
                .decidedByUserId(claim.getDecidedByUserId())
                .decidedAt(claim.getDecidedAt())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
