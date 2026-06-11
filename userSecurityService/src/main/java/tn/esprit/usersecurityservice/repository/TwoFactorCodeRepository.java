package tn.esprit.usersecurityservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.usersecurityservice.entity.TwoFactorCode;
import tn.esprit.usersecurityservice.entity.User;

import java.util.Optional;

@Repository
public interface TwoFactorCodeRepository extends JpaRepository<TwoFactorCode, Long> {
    Optional<TwoFactorCode> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    void deleteByUser(User user);
}
