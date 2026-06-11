package tn.esprit.eventservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventservice.dto.EventRatingDTO;
import tn.esprit.eventservice.service.EventRatingServiceImpl;

@RestController
@RequestMapping("/api/ratings")
public class EventRatingController {

    private final EventRatingServiceImpl ratingService;

    public EventRatingController(EventRatingServiceImpl ratingService) {
        this.ratingService = ratingService;
    }

    // POST /api/ratings → noter ou modifier
    @PostMapping
    public ResponseEntity<EventRatingDTO> rate(@Valid @RequestBody EventRatingDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.rateEvent(dto));
    }

    @GetMapping("/event/{eventId}/stats")
    public ResponseEntity<EventRatingDTO> getStats(@PathVariable("eventId") Long eventId) {
        return ResponseEntity.ok(ratingService.getEventRatingStats(eventId));
    }

    @GetMapping("/event/{eventId}/user/{userId}")
    public ResponseEntity<EventRatingDTO> getUserRating(@PathVariable("eventId") Long eventId,
                                                        @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(ratingService.getUserRating(eventId, userId));
    }

    @DeleteMapping("/event/{eventId}/user/{userId}")
    public ResponseEntity<Void> delete(@PathVariable("eventId") Long eventId,
                                       @PathVariable("userId") Long userId) {
        ratingService.deleteRating(eventId, userId);
        return ResponseEntity.noContent().build();
    }
}
