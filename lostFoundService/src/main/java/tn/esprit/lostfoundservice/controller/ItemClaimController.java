package tn.esprit.lostfoundservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.lostfoundservice.DTO.ItemClaimDecisionDTO;
import tn.esprit.lostfoundservice.DTO.ItemClaimRequestDTO;
import tn.esprit.lostfoundservice.DTO.ItemClaimResponseDTO;
import tn.esprit.lostfoundservice.exception.UnauthorizedAccessException;
import tn.esprit.lostfoundservice.service.ItemClaimService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
public class ItemClaimController {

    private final ItemClaimService itemClaimService;

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new UnauthorizedAccessException("User identity is required");
        }
        return userId;
    }

    @PostMapping("/items/{itemId}")
    public ResponseEntity<ItemClaimResponseDTO> createClaim(
            @PathVariable Long itemId,
            @Valid @RequestBody ItemClaimRequestDTO requestDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        userId = requireUserId(userId);
        ItemClaimResponseDTO response = itemClaimService.createClaim(itemId, userId, requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ItemClaimResponseDTO>> getMyClaims(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        userId = requireUserId(userId);
        return ResponseEntity.ok(itemClaimService.getMyClaims(userId));
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<List<ItemClaimResponseDTO>> getClaimsForMyItem(
            @PathVariable Long itemId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        userId = requireUserId(userId);
        return ResponseEntity.ok(itemClaimService.getClaimsForMyItem(itemId, userId));
    }

    @PatchMapping("/{claimId}/approve")
    public ResponseEntity<ItemClaimResponseDTO> approveClaim(
            @PathVariable Long claimId,
            @RequestBody(required = false) ItemClaimDecisionDTO decisionDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        userId = requireUserId(userId);
        ItemClaimDecisionDTO payload = decisionDTO == null ? new ItemClaimDecisionDTO() : decisionDTO;
        return ResponseEntity.ok(itemClaimService.approveClaim(claimId, userId, payload));
    }

    @PatchMapping("/{claimId}/reject")
    public ResponseEntity<ItemClaimResponseDTO> rejectClaim(
            @PathVariable Long claimId,
            @RequestBody(required = false) ItemClaimDecisionDTO decisionDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        userId = requireUserId(userId);
        ItemClaimDecisionDTO payload = decisionDTO == null ? new ItemClaimDecisionDTO() : decisionDTO;
        return ResponseEntity.ok(itemClaimService.rejectClaim(claimId, userId, payload));
    }

    @PatchMapping("/{claimId}/cancel")
    public ResponseEntity<ItemClaimResponseDTO> cancelMyClaim(
            @PathVariable Long claimId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        userId = requireUserId(userId);
        return ResponseEntity.ok(itemClaimService.cancelMyClaim(claimId, userId));
    }
}
