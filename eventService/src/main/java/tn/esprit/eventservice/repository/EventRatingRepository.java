package tn.esprit.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.eventservice.entity.EventRating;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRatingRepository extends JpaRepository<EventRating, Long> {

    Optional<EventRating> findByEventIdAndUserId(Long eventId, Long userId);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    List<EventRating> findByEventId(Long eventId);

    @Query("SELECT AVG(r.rating) FROM EventRating r WHERE r.event.id = :eventId")
    Double findAverageRatingByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM EventRating r WHERE r.event.id = :eventId")
    Long countByEventId(@Param("eventId") Long eventId);
}
