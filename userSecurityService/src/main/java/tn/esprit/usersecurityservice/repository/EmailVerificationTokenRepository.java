package tn.esprit.usersecurityservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.usersecurityservice.entity.EmailVerificationToken;
import tn.esprit.usersecurityservice.entity.User;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByUserId(Long userId);
    Optional<EmailVerificationToken> findByUser(User user);
    boolean existsByUserId(Long userId);
    void deleteByUser(User user);
}
