package tn.esprit.eventservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventservice.dto.PagedResponse;
import tn.esprit.eventservice.dto.ScoredEventDTO;
import tn.esprit.eventservice.service.RecommendationService;

@RestController
@RequestMapping("/api/events/recommended")
public class EventRecommendationController {

    private final RecommendationService recommendationService;

    public EventRecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<PagedResponse<ScoredEventDTO>> getRecommendedEvents(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ScoredEventDTO> result = recommendationService.getRecommendedEvents(userId, pageable);

        PagedResponse<ScoredEventDTO> response = new PagedResponse<>();
        response.setContent(result.getContent());
        response.setPage(result.getNumber());
        response.setSize(result.getSize());
        response.setTotalElements(result.getTotalElements());
        response.setTotalPages(result.getTotalPages());
        response.setLast(result.isLast());

        return ResponseEntity.ok(response);
    }
}
