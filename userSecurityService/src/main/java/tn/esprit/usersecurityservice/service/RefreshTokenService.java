package tn.esprit.usersecurityservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.usersecurityservice.entity.RefreshToken;
import tn.esprit.usersecurityservice.entity.User;
import tn.esprit.usersecurityservice.repository.RefreshTokenRepository;
import tn.esprit.usersecurityservice.repository.UserRepository;
import tn.esprit.usersecurityservice.validation.Validators;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        Validators.requireNonBlank(token, "token");
        Validators.requireMaxLength(token, Validators.MAX_TOKEN_LENGTH, "token");
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        Validators.requirePositive(userId, "userId");
        return createOrUpdateRefreshToken(userId);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        Validators.requirePositive(userId, "userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void deleteByToken(String token) {
        Validators.requireNonBlank(token, "token");
        Validators.requireMaxLength(token, Validators.MAX_TOKEN_LENGTH, "token");
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Rotates the refresh token: revokes the old one and creates a new one.
     */
    @Transactional
    public RefreshToken rotateRefreshToken(String oldToken) {
        Validators.requireNonBlank(oldToken, "refreshToken");
        Validators.requireMaxLength(oldToken, Validators.MAX_TOKEN_LENGTH, "refreshToken");
        Optional<RefreshToken> existingToken = findByToken(oldToken);
        if (existingToken.isPresent()) {
            RefreshToken token = verifyExpiration(existingToken.get());
            token.setToken(UUID.randomUUID().toString());
            token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            return refreshTokenRepository.save(token);
        } else {
            throw new RuntimeException("Refresh token not found!");
        }
    }

    public RefreshToken createOrUpdateRefreshToken(Long userId) {
        Validators.requirePositive(userId, "userId");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .orElseGet(() -> {
                    RefreshToken rt = new RefreshToken();
                    rt.setUser(user);
                    return rt;
                });

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        return refreshTokenRepository.save(refreshToken);
    }
}
