package tn.esprit.realestateservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.realestateservice.entity.Furniture;
import tn.esprit.realestateservice.service.FurnitureService;

import java.util.List;

@RestController
@RequestMapping("/api/furniture")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FurnitureController {

    private final FurnitureService furnitureService;

    @PostMapping
    public ResponseEntity<Furniture> create(@RequestBody Furniture furniture) {
        Furniture created = furnitureService.create(furniture);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Furniture>> getAll() {
        return ResponseEntity.ok(furnitureService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Furniture> getById(@PathVariable Long id) {
        return ResponseEntity.ok(furnitureService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Furniture> update(@PathVariable Long id, @RequestBody Furniture furniture) {
        return ResponseEntity.ok(furnitureService.update(id, furniture));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        furnitureService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
