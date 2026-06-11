package tn.esprit.usersecurityservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.usersecurityservice.entity.UserProfile;

import java.util.Optional;

public interface UserProfileRepository
        extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);
}