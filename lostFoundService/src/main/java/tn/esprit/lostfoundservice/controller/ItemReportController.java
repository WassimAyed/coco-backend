package tn.esprit.lostfoundservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.lostfoundservice.DTO.ItemReportRequestDTO;
import tn.esprit.lostfoundservice.DTO.ItemReportResponseDTO;
import tn.esprit.lostfoundservice.DTO.ItemReportReviewDTO;
import tn.esprit.lostfoundservice.entity.ReportStatus;
import tn.esprit.lostfoundservice.exception.UnauthorizedAccessException;
import tn.esprit.lostfoundservice.service.ItemReportService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ItemReportController {

    private final ItemReportService itemReportService;

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new UnauthorizedAccessException("User identity is required");
        }
        return userId;
    }

    private void requireModeratorRole(String role) {
        if (role == null || (!"ADMIN".equalsIgnoreCase(role) && !"MODERATOR".equalsIgnoreCase(role))) {
            throw new UnauthorizedAccessException("Moderator role is required");
        }
    }

    @PostMapping("/items/{itemId}")
    public ResponseEntity<ItemReportResponseDTO> createReport(
            @PathVariable Long itemId,
            @Valid @RequestBody ItemReportRequestDTO requestDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        userId = requireUserId(userId);
        ItemReportResponseDTO response = itemReportService.createReport(itemId, userId, requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ItemReportResponseDTO>> getMyReports(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        userId = requireUserId(userId);
        return ResponseEntity.ok(itemReportService.getMyReports(userId));
    }

    @GetMapping("/owner/my-items")
    public ResponseEntity<List<ItemReportResponseDTO>> getReportsForMyItems(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        userId = requireUserId(userId);
        return ResponseEntity.ok(itemReportService.getReportsForMyItems(userId));
    }

    @GetMapping
    public ResponseEntity<List<ItemReportResponseDTO>> getReportsForModeration(
            @RequestParam(required = false) ReportStatus status,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        requireModeratorRole(role);
        return ResponseEntity.ok(itemReportService.getReportsByStatus(status));
    }

    @PatchMapping("/{reportId}/review")
    public ResponseEntity<ItemReportResponseDTO> reviewReport(
            @PathVariable Long reportId,
            @Valid @RequestBody ItemReportReviewDTO reviewDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        userId = requireUserId(userId);
        requireModeratorRole(role);
        return ResponseEntity.ok(itemReportService.reviewReport(reportId, userId, reviewDTO));
    }
}
