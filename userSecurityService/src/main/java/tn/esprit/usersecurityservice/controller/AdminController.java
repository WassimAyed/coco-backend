package tn.esprit.usersecurityservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.usersecurityservice.dto.UserResponse;
import tn.esprit.usersecurityservice.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        adminService.setEnabled(id, false);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        adminService.setEnabled(id, true);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
