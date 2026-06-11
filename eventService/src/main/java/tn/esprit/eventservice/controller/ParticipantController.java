package tn.esprit.eventservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventservice.dto.ParticipantDTO;
import tn.esprit.eventservice.service.IParticipantService;

import java.util.List;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    private final IParticipantService participantService;

    public ParticipantController(IParticipantService participantService) {
        this.participantService = participantService;
    }

    @PostMapping
    public ResponseEntity<ParticipantDTO> register(@Valid @RequestBody ParticipantDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(participantService.registerParticipant(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unregister(@PathVariable("id") Long id) {
        participantService.unregisterParticipant(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParticipantDTO> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(participantService.getParticipantById(id));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ParticipantDTO>> getByEvent(@PathVariable("eventId") Long eventId) {
        return ResponseEntity.ok(participantService.getParticipantsByEvent(eventId));
    }

    @GetMapping("/event/{eventId}/count")
    public ResponseEntity<Long> countByEvent(@PathVariable("eventId") Long eventId) {
        return ResponseEntity.ok(participantService.countParticipantsByEvent(eventId));
    }
}