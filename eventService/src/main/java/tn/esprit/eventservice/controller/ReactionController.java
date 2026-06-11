package tn.esprit.eventservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventservice.dto.ReactionDTO;
import tn.esprit.eventservice.dto.ReactionSummaryDTO;
import tn.esprit.eventservice.service.IReactionService;

@RestController
@RequestMapping("/api/reactions")
public class ReactionController {

    private final IReactionService reactionService;

    public ReactionController(IReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @Operation(summary = "Ajouter ou modifier une réaction")
    @PostMapping
    public ResponseEntity<ReactionDTO> addOrUpdate(@Valid @RequestBody ReactionDTO dto) {
        return ResponseEntity.ok(reactionService.addOrUpdateReaction(dto));
    }

    @Operation(summary = "Supprimer une réaction")
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<Void> remove(
            @PathVariable("eventId") Long eventId,
            @RequestParam String authorEmail) {
        reactionService.removeReaction(eventId, authorEmail);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Résumé des réactions d'un événement")
    @GetMapping("/event/{eventId}/summary")
    public ResponseEntity<ReactionSummaryDTO> getSummary(@PathVariable("eventId") Long eventId) {
        return ResponseEntity.ok(reactionService.getReactionSummary(eventId));
    }
}
