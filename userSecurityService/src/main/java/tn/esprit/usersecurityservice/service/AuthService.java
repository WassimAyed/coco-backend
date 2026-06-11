package tn.esprit.usersecurityservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.usersecurityservice.Enum.Role;
import tn.esprit.usersecurityservice.dto.JwtResponse;
import tn.esprit.usersecurityservice.dto.LoginRequest;
import tn.esprit.usersecurityservice.dto.LoginResponse;
import tn.esprit.usersecurityservice.dto.MessageResponse;
import tn.esprit.usersecurityservice.dto.ResendVerificationCodeRequest;
import tn.esprit.usersecurityservice.dto.RegisterRequest;
import tn.esprit.usersecurityservice.dto.TokenRefreshRequest;
import tn.esprit.usersecurityservice.dto.VerifyEmailRequest;
import tn.esprit.usersecurityservice.dto.VerifyTwoFactorRequest;
import tn.esprit.usersecurityservice.entity.EmailVerificationToken;
import tn.esprit.usersecurityservice.entity.RefreshToken;
import tn.esprit.usersecurityservice.entity.TwoFactorCode;
import tn.esprit.usersecurityservice.entity.User;
import tn.esprit.usersecurityservice.exception.AccountNotVerifiedException;
import tn.esprit.usersecurityservice.exception.ConflictException;
import tn.esprit.usersecurityservice.exception.NotFoundException;
import tn.esprit.usersecurityservice.exception.TooManyRequestsException;
import tn.esprit.usersecurityservice.repository.EmailVerificationTokenRepository;
import tn.esprit.usersecurityservice.repository.TwoFactorCodeRepository;
import tn.esprit.usersecurityservice.repository.UserRepository;
import tn.esprit.usersecurityservice.security.JwtService;
import tn.esprit.usersecurityservice.validation.Validators;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final TwoFactorCodeRepository twoFactorCodeRepository;
    private final EmailService emailService;

    @Value("${app.email-verification.expiration-minutes}")
    private long verificationExpirationMinutes;

    @Value("${app.email-verification.max-attempts}")
    private int maxVerificationAttempts;

    @Value("${app.email-verification.resend-cooldown-seconds}")
    private long resendCooldownSeconds;

    @Value("${app.two-factor.expiration-minutes}")
    private long twoFactorExpirationMinutes;

    @Value("${app.two-factor.max-attempts}")
    private int maxTwoFactorAttempts;

    @Value("${app.two-factor.resend-cooldown-seconds}")
    private long twoFactorResendCooldownSeconds;

    public MessageResponse register(RegisterRequest request) {
        Validators.requireNonNull(request, "request");
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already in use");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        Role role = Role.USER;

        var user = User.builder()
                .username(request.getUsername())
                .lastname(request.getLastname())
                .email(request.getEmail())
            .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(false)
                .build();

        userRepository.save(user);
        generateAndSendVerificationLink(user, true);

        return new MessageResponse("Registration successful. A verification link was sent to your email.");
    }

    public LoginResponse login(LoginRequest request) {
        Validators.requireNonNull(request, "request");
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            if (!user.isEnabled()) {
                if (emailVerificationTokenRepository.existsByUserId(user.getId())) {
                    throw new AccountNotVerifiedException("Email not verified. Please verify your account before logging in.");
                }
                throw new DisabledException("Account is disabled");
            }
        });

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isTwoFactorEnabled()) {
            generateAndSendTwoFactorCode(user, true);
            return LoginResponse.twoFactorRequired("A login verification code was sent to your email.");
        }

        return buildLoginSuccessResponse(user);
    }

    public JwtResponse refreshToken(TokenRefreshRequest request) {
        Validators.requireNonNull(request, "request");
        String requestRefreshToken = request.getRefreshToken();
        Validators.requireNonBlank(requestRefreshToken, "refreshToken");
        Validators.requireMaxLength(requestRefreshToken, Validators.MAX_TOKEN_LENGTH, "refreshToken");

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(requestRefreshToken);
        User user = newRefreshToken.getUser();
        String token = jwtService.generateToken(user);

        return new JwtResponse(token, newRefreshToken.getToken());
    }

    public MessageResponse verifyEmail(VerifyEmailRequest request) {
        Validators.requireNonNull(request, "request");
        return verifyEmail(request.getEmail(), request.getToken());
    }

    public MessageResponse verifyEmail(String email, String token) {
        Validators.requireNonBlank(email, "email");
        Validators.requireMaxLength(email, Validators.MAX_EMAIL_LENGTH, "email");
        Validators.requireNonBlank(token, "token");
        Validators.requireMaxLength(token, Validators.MAX_TOKEN_LENGTH, "token");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isEnabled() && emailVerificationTokenRepository.findByUserId(user.getId()).isEmpty()) {
            return new MessageResponse("Email is already verified.");
        }

        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Verification link not found"));

        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            emailVerificationTokenRepository.delete(verificationToken);
            throw new IllegalArgumentException("Verification link expired. Request a new link.");
        }

        if (!verificationToken.getToken().equals(token)) {
            verificationToken.setFailedAttempts(verificationToken.getFailedAttempts() + 1);

            if (verificationToken.getFailedAttempts() >= maxVerificationAttempts) {
                emailVerificationTokenRepository.delete(verificationToken);
                throw new TooManyRequestsException("Maximum verification attempts reached. Request a new link.");
            }

            emailVerificationTokenRepository.save(verificationToken);
            throw new IllegalArgumentException("Invalid verification link.");
        }

        user.setEnabled(true);
        userRepository.save(user);
        emailVerificationTokenRepository.delete(verificationToken);

        return new MessageResponse("Email verified successfully. You can now log in.");
    }

    public MessageResponse resendVerificationCode(ResendVerificationCodeRequest request) {
        Validators.requireNonNull(request, "request");
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isEnabled() && emailVerificationTokenRepository.findByUserId(user.getId()).isEmpty()) {
            return new MessageResponse("Email is already verified.");
        }

        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByUserId(user.getId())
                .orElse(null);

        if (verificationToken != null && verificationToken.getResendAvailableAt().isAfter(Instant.now())) {
            throw new TooManyRequestsException("Please wait before requesting another verification link.");
        }

        generateAndSendVerificationLink(user, true);
        return new MessageResponse("A new verification link was sent to your email.");
    }

    public LoginResponse verifyTwoFactor(VerifyTwoFactorRequest request) {
        Validators.requireNonNull(request, "request");
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isTwoFactorEnabled()) {
            throw new IllegalArgumentException("Two-factor authentication is not enabled for this account.");
        }

        TwoFactorCode twoFactorCode = twoFactorCodeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Two-factor code not found"));

        if (twoFactorCode.getExpiresAt().isBefore(Instant.now())) {
            twoFactorCodeRepository.delete(twoFactorCode);
            throw new IllegalArgumentException("Two-factor code expired. Request a new code.");
        }

        if (!twoFactorCode.getCode().equals(request.getCode())) {
            twoFactorCode.setFailedAttempts(twoFactorCode.getFailedAttempts() + 1);

            if (twoFactorCode.getFailedAttempts() >= maxTwoFactorAttempts) {
                twoFactorCodeRepository.delete(twoFactorCode);
                throw new TooManyRequestsException("Maximum 2FA attempts reached. Log in again to get a new code.");
            }

            twoFactorCodeRepository.save(twoFactorCode);
            throw new IllegalArgumentException("Invalid two-factor code.");
        }

        twoFactorCodeRepository.delete(twoFactorCode);
        return buildLoginSuccessResponse(user);
    }

    public MessageResponse resendTwoFactorCode(ResendVerificationCodeRequest request) {
        Validators.requireNonNull(request, "request");
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isTwoFactorEnabled()) {
            throw new IllegalArgumentException("Two-factor authentication is not enabled for this account.");
        }

        TwoFactorCode twoFactorCode = twoFactorCodeRepository.findByUserId(user.getId())
                .orElse(null);

        if (twoFactorCode != null && twoFactorCode.getResendAvailableAt().isAfter(Instant.now())) {
            throw new TooManyRequestsException("Please wait before requesting another 2FA code.");
        }

        generateAndSendTwoFactorCode(user, true);
        return new MessageResponse("A new two-factor code was sent to your email.");
    }

    public void logout(String refreshToken) {
        Validators.requireNonBlank(refreshToken, "refreshToken");
        Validators.requireMaxLength(refreshToken, Validators.MAX_TOKEN_LENGTH, "refreshToken");
        refreshTokenService.deleteByToken(refreshToken);
    }

    public void removeTwoFactorCode(User user) {
        twoFactorCodeRepository.deleteByUser(user);
    }

    private void generateAndSendVerificationLink(User user, boolean createIfMissing) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    if (!createIfMissing) {
                        throw new NotFoundException("Verification link not found");
                    }

                    EmailVerificationToken newToken = new EmailVerificationToken();
                    newToken.setUser(user);
                    return newToken;
                });

        verificationToken.setToken(generateEmailVerificationToken());
        verificationToken.setExpiresAt(Instant.now().plusSeconds(verificationExpirationMinutes * 60));
        verificationToken.setFailedAttempts(0);
        verificationToken.setResendAvailableAt(Instant.now().plusSeconds(resendCooldownSeconds));

        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendEmailVerificationLink(user.getEmail(), user.getUsername(), verificationToken.getToken());
    }

    private void generateAndSendTwoFactorCode(User user, boolean createIfMissing) {
        TwoFactorCode twoFactorCode = twoFactorCodeRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    if (!createIfMissing) {
                        throw new NotFoundException("Two-factor code not found");
                    }

                    TwoFactorCode newCode = new TwoFactorCode();
                    newCode.setUser(user);
                    return newCode;
                });

        twoFactorCode.setCode(generateOtpCode());
        twoFactorCode.setExpiresAt(Instant.now().plusSeconds(twoFactorExpirationMinutes * 60));
        twoFactorCode.setFailedAttempts(0);
        twoFactorCode.setResendAvailableAt(Instant.now().plusSeconds(twoFactorResendCooldownSeconds));

        twoFactorCodeRepository.save(twoFactorCode);
        emailService.sendTwoFactorOtp(user.getEmail(), user.getUsername(), twoFactorCode.getCode());
    }

    private LoginResponse buildLoginSuccessResponse(User user) {
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createOrUpdateRefreshToken(user.getId());
        return LoginResponse.tokens(jwtToken, refreshToken.getToken());
    }

    private String generateOtpCode() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    private String generateEmailVerificationToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
