package tn.esprit.lostfoundservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.lostfoundservice.entity.ClaimStatus;
import tn.esprit.lostfoundservice.entity.ItemClaim;

import java.util.List;

@Repository
public interface ItemClaimRepository extends JpaRepository<ItemClaim, Long> {
    List<ItemClaim> findByClaimantUserIdOrderByCreatedAtDesc(Long claimantUserId);
    List<ItemClaim> findByItemIdOrderByCreatedAtDesc(Long itemId);
    boolean existsByItemIdAndClaimantUserIdAndStatus(Long itemId, Long claimantUserId, ClaimStatus status);
}
