package tn.esprit.collocationservice.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import tn.esprit.collocationservice.Entity.collocOffreFavorite;

import java.util.List;
import java.util.Optional;

public interface collocOffreFavoriteRepo extends JpaRepository<collocOffreFavorite, Long> {
    List<collocOffreFavorite> findByUserId(Long userId);
    Optional<collocOffreFavorite> findByUserIdAndOffreId(Long userId, Long offreId);

    @Modifying
    @Transactional
    void deleteByUserIdAndOffreId(Long userId, Long offreId);
}