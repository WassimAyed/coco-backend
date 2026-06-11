package com.example.realestateservice.controller;

import com.example.realestateservice.dto.FurnitureDTO;
import com.example.realestateservice.entity.enums.Status;
import com.example.realestateservice.service.FurnitureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/furniture")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FurnitureController {

    private final FurnitureService furnitureService;

    @PostMapping
    public ResponseEntity<FurnitureDTO> create(@Valid @RequestBody FurnitureDTO dto) {
        FurnitureDTO created = furnitureService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<FurnitureDTO>> getAll() {
        return ResponseEntity.ok(furnitureService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FurnitureDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(furnitureService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FurnitureDTO> update(@PathVariable Long id, @Valid @RequestBody FurnitureDTO dto) {
        return ResponseEntity.ok(furnitureService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        furnitureService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<FurnitureDTO>> getBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(furnitureService.findBySellerId(sellerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FurnitureDTO>> getByStatus(@PathVariable Status status) {
        return ResponseEntity.ok(furnitureService.findByStatus(status));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<FurnitureDTO>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(furnitureService.findByCategory(category));
    }
}
