package tn.esprit.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.eventservice.entity.Reaction;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    List<Reaction> findByEventId(Long eventId);
    Optional<Reaction> findByEventIdAndAuthorEmail(Long eventId, String authorEmail);
    boolean existsByEventIdAndAuthorEmail(Long eventId, String authorEmail);
    long countByEventId(Long eventId);

    @Query("SELECT r.type, COUNT(r) FROM Reaction r WHERE r.event.id = :eventId GROUP BY r.type")
    List<Object[]> countByEventIdGroupByType(@Param("eventId") Long eventId);
}