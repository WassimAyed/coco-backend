package tn.esprit.usersecurityservice.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.usersecurityservice.dto.ImageUploadResponse;
import tn.esprit.usersecurityservice.dto.PasswordUpdateRequest;
import tn.esprit.usersecurityservice.dto.UserResponse;
import tn.esprit.usersecurityservice.dto.UserUpdateRequest;
import tn.esprit.usersecurityservice.dto.MessageResponse;
import tn.esprit.usersecurityservice.entity.User;
import tn.esprit.usersecurityservice.mapper.UserMapper;
import tn.esprit.usersecurityservice.repository.EmailVerificationTokenRepository;
import tn.esprit.usersecurityservice.repository.UserRepository;
import tn.esprit.usersecurityservice.security.JwtService;
import tn.esprit.usersecurityservice.validation.Validators;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final AuthService authService;
    private final StorageGatewayService storageGatewayService;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null || authentication instanceof AnonymousAuthenticationToken) {
            return resolveUserFromBearerToken()
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user && user.getId() != null) {
            return userRepository.findById(user.getId())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByEmailOrUsername(userDetails.getUsername())
                    .or(() -> userRepository.findByEmailOrUsername(authentication.getName()))
                    .or(this::resolveUserFromBearerToken)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        return userRepository.findByEmailOrUsername(authentication.getName())
                .or(() -> userRepository.findByEmailOrUsername(principal.toString()))
                .or(this::resolveUserFromBearerToken)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private java.util.Optional<User> resolveUserFromBearerToken() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return java.util.Optional.empty();
        }

        HttpServletRequest request = attributes.getRequest();
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return java.util.Optional.empty();
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isBlank()) {
            return java.util.Optional.empty();
        }

        Long userId = jwtService.extractUserId(token);
        if (userId != null) {
            return userRepository.findById(userId);
        }

        String subject = jwtService.extractUsername(token);
        return userRepository.findByEmailOrUsername(subject);
    }

    public UserResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        return userMapper.toResponse(user);
    }

    public UserResponse getUserProfile(Long id) {
        Validators.requirePositive(id, "id");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UserUpdateRequest request) {
        Validators.requireNonNull(request, "request");
        Validators.requireMaxLength(request.getUsername(), Validators.MAX_USERNAME_LENGTH, "username");
        Validators.requireMaxLength(request.getLastname(), Validators.MAX_LASTNAME_LENGTH, "lastname");
        Validators.requireMaxLength(request.getImageUrl(), Validators.MAX_IMAGE_URL_LENGTH, "imageUrl");
        User user = getCurrentUser();

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username already taken");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getLastname() != null && !request.getLastname().isBlank()) {
            user.setLastname(request.getLastname());
        }
        if (request.getImageUrl() != null) {
            String normalizedImageUrl = request.getImageUrl().trim();
            if (!normalizedImageUrl.isBlank()) {
                user.setImageUrl(normalizedImageUrl);
            }
        }

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public void updatePassword(PasswordUpdateRequest request) {
        Validators.requireNonNull(request, "request");
        Validators.requireNonBlank(request.getOldPassword(), "oldPassword");
        Validators.requireNonBlank(request.getNewPassword(), "newPassword");
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong current password");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount() {
        User user = getCurrentUser();
        refreshTokenService.deleteByUserId(user.getId());
        authService.removeTwoFactorCode(user);
        emailVerificationTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    @Transactional
    public MessageResponse setTwoFactorEnabled(boolean enabled) {
        User user = getCurrentUser();
        user.setTwoFactorEnabled(enabled);
        userRepository.save(user);

        if (!enabled) {
            authService.removeTwoFactorCode(user);
            return new MessageResponse("Two-factor authentication disabled.");
        }

        return new MessageResponse("Two-factor authentication enabled.");
    }

    @Transactional
    public ImageUploadResponse uploadProfileImage(MultipartFile image) {
        Validators.requireImage(image, Validators.MAX_IMAGE_BYTES);
        User user = getCurrentUser();
        ImageUploadResponse upload = storageGatewayService.uploadProfileImage(image, user.getId());
        user.setImageUrl(upload.imageUrl());
        userRepository.save(user);
        return upload;
    }

}
