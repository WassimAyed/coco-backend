package tn.esprit.eventservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventservice.dto.CommentDTO;
import tn.esprit.eventservice.service.ICommentService;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final ICommentService commentService;

    public CommentController(ICommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Ajouter un commentaire à un événement")
    @PostMapping
    public ResponseEntity<CommentDTO> addComment(@Valid @RequestBody CommentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(dto));
    }

    @Operation(summary = "Modifier un commentaire")
    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable("id") Long id,
            @Valid @RequestBody CommentDTO dto) {
        return ResponseEntity.ok(commentService.updateComment(id, dto));
    }

    @Operation(summary = "Supprimer un commentaire")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable("id") Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Récupérer les commentaires d'un événement")
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<CommentDTO>> getByEvent(@PathVariable("eventId") Long eventId) {
        return ResponseEntity.ok(commentService.getCommentsByEvent(eventId));
    }

    @Operation(summary = "Compter les commentaires d'un événement")
    @GetMapping("/event/{eventId}/count")
    public ResponseEntity<Long> countByEvent(@PathVariable("eventId") Long eventId) {
        return ResponseEntity.ok(commentService.countCommentsByEvent(eventId));
    }
}