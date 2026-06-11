package tn.esprit.realestateservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.realestateservice.dto.FurnitureImageDTO;
import tn.esprit.realestateservice.service.FurnitureImageService;

import java.util.List;

@RestController
@RequestMapping("/api/furniture-images")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FurnitureImageController {

    private final FurnitureImageService furnitureImageService;

    @PostMapping
    public ResponseEntity<FurnitureImageDTO> create(@Valid @RequestBody FurnitureImageDTO dto) {
        FurnitureImageDTO created = furnitureImageService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<FurnitureImageDTO>> getAll() {
        return ResponseEntity.ok(furnitureImageService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FurnitureImageDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(furnitureImageService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FurnitureImageDTO> update(@PathVariable Long id, @Valid @RequestBody FurnitureImageDTO dto) {
        return ResponseEntity.ok(furnitureImageService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        furnitureImageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
