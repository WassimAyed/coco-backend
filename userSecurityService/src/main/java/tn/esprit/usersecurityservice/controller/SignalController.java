package tn.esprit.usersecurityservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.usersecurityservice.dto.SignalResponse;
import tn.esprit.usersecurityservice.service.SignalService;

import java.util.List;

@RestController
@RequestMapping("/signals")
@RequiredArgsConstructor
public class SignalController {
    private final SignalService signalService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<SignalResponse> create(
            @RequestPart("description") String description,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ResponseEntity.status(201).body(signalService.create(description, image));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SignalResponse> getAll() {
        return signalService.getAll();
    }

    @GetMapping("/me")
    public List<SignalResponse> getMine() {
        return signalService.getMine();
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<SignalResponse> update(
            @PathVariable Long id,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ResponseEntity.ok(signalService.update(id, description, image));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        signalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
