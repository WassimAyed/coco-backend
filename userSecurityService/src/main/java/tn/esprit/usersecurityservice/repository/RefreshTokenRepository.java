package tn.esprit.usersecurityservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.usersecurityservice.entity.RefreshToken;
import tn.esprit.usersecurityservice.entity.User;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserId(Long userId);
    int deleteByUser(User user);

    Long user(User user);
}
