package tn.esprit.usersecurityservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.usersecurityservice.dto.ProfileRequestDTO;
import tn.esprit.usersecurityservice.entity.UserProfile;
import tn.esprit.usersecurityservice.service.UserProfileService;

import java.util.List;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService service;

    // ✅ GET profile
    @GetMapping("/{userId}")
    public UserProfile getProfile(@PathVariable Long userId) {
        return service.getByUserId(userId);
    }

    // ✅ GET all
    @GetMapping("/all")
    public List<UserProfile> getAllProfiles() {
        return service.getAllProfiles();
    }

    @PostMapping
    public ResponseEntity<?> createProfile(
            @RequestBody ProfileRequestDTO request
    ) {

        UserProfile profile = service.save(request);

        return ResponseEntity.ok().body(
                java.util.Map.of(
                        "message", "PROFILE_CREATED",
                        "profileId", profile.getId()
                )
        );
    }

    @GetMapping("/exists/{userId}")
    public boolean profileExists(@PathVariable Long userId) {
        return service.existsByUserId(userId);
    }

    // ✅ UPDATE profile
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @RequestBody ProfileRequestDTO request
    ) {

        UserProfile updatedProfile = service.updateProfile(userId, request);

        return ResponseEntity.ok().body(
                java.util.Map.of(
                        "message", "PROFILE_UPDATED",
                        "profileId", updatedProfile.getId()
                )
        );
    }
}