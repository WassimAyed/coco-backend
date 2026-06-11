package tn.esprit.realestateservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.realestateservice.dto.AddressDTO;
import tn.esprit.realestateservice.service.AddressService;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressDTO> create(@Valid @RequestBody AddressDTO dto) {
        AddressDTO created = addressService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAll() {
        return ResponseEntity.ok(addressService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> update(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        return ResponseEntity.ok(addressService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
