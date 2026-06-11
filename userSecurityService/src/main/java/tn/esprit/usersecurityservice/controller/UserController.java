package tn.esprit.usersecurityservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.usersecurityservice.dto.ImageUploadResponse;
import tn.esprit.usersecurityservice.dto.PasswordUpdateRequest;
import tn.esprit.usersecurityservice.dto.UserResponse;
import tn.esprit.usersecurityservice.dto.UserUpdateRequest;
import tn.esprit.usersecurityservice.dto.MessageResponse;
import tn.esprit.usersecurityservice.dto.ToggleTwoFactorRequest;
import tn.esprit.usersecurityservice.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")  // allow Angular frontend
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateUser(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount() {
        userService.deleteAccount();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/two-factor")
    public ResponseEntity<MessageResponse> setTwoFactor(@RequestBody ToggleTwoFactorRequest request) {
        return ResponseEntity.ok(userService.setTwoFactorEnabled(request.isEnabled()));
    }

    @PostMapping(value = "/me/profile-image", consumes = "multipart/form-data")
    public ResponseEntity<ImageUploadResponse> uploadProfileImage(
            @RequestPart("image") MultipartFile image
    ) {
        return ResponseEntity.ok(userService.uploadProfileImage(image));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }
}
