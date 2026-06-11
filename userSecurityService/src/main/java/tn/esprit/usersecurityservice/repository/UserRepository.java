package tn.esprit.usersecurityservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.usersecurityservice.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User , Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameIgnoreCase(String username);
    default Optional<User> findByEmailOrUsername(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        String normalizedIdentifier = identifier.trim();

        return findByEmail(normalizedIdentifier)
                .or(() -> findByUsername(normalizedIdentifier))
                .or(() -> findByEmailIgnoreCase(normalizedIdentifier))
                .or(() -> findByUsernameIgnoreCase(normalizedIdentifier));
    }
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);

    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :id")
    int updateEnabledById(@Param("id") Long id, @Param("enabled") boolean enabled);
}
