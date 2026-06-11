package tn.esprit.eventservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventservice.dto.SimilarEventDTO;
import tn.esprit.eventservice.service.SimilarEventServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class SimilarEventController {

    private final SimilarEventServiceImpl similarEventService;

    public SimilarEventController(SimilarEventServiceImpl similarEventService) {
        this.similarEventService = similarEventService;
    }

    // GET /api/events/{id}/similar?limit=5
    @GetMapping("/{id}/similar")
    public ResponseEntity<List<SimilarEventDTO>> getSimilar(
            @PathVariable("id") Long id,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {

        return ResponseEntity.ok(similarEventService.getSimilarEvents(id, limit));
    }
}