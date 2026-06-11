package tn.esprit.usersecurityservice.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import tn.esprit.usersecurityservice.dto.LoginRequest;
import tn.esprit.usersecurityservice.dto.LoginResponse;
import tn.esprit.usersecurityservice.dto.MessageResponse;
import tn.esprit.usersecurityservice.dto.ResendVerificationCodeRequest;
import tn.esprit.usersecurityservice.dto.RegisterRequest;
import tn.esprit.usersecurityservice.dto.TokenRefreshRequest;
import tn.esprit.usersecurityservice.dto.VerifyEmailRequest;
import tn.esprit.usersecurityservice.dto.VerifyTwoFactorRequest;
import tn.esprit.usersecurityservice.dto.JwtResponse;
import tn.esprit.usersecurityservice.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(authService.verifyEmail(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmailByLink(
            @RequestParam String email,
            @RequestParam String token
    ) {
        return ResponseEntity.ok(authService.verifyEmail(email, token));
    }

    @PostMapping("/resend-verification-code")
    public ResponseEntity<MessageResponse> resendVerificationCode(@Valid @RequestBody ResendVerificationCodeRequest request) {
        return ResponseEntity.ok(authService.resendVerificationCode(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<LoginResponse> verifyTwoFactor(@Valid @RequestBody VerifyTwoFactorRequest request) {
        return ResponseEntity.ok(authService.verifyTwoFactor(request));
    }

    @PostMapping("/resend-2fa-code")
    public ResponseEntity<MessageResponse> resendTwoFactorCode(@Valid @RequestBody ResendVerificationCodeRequest request) {
        return ResponseEntity.ok(authService.resendTwoFactorCode(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-session")
    public ResponseEntity<Void> logoutSession(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, null);
        return ResponseEntity.noContent().build();
    }
}
