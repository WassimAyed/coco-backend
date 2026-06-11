package tn.esprit.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByUserId(Long userId, Pageable pageable);

    // Recherche par statut
    List<Event> findByStatus(EventStatus status);
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    // Recherche par catégorie
    List<Event> findByCategoryId(Long categoryId);
    Page<Event> findByCategoryId(Long categoryId, Pageable pageable);

    // Recherche par nom (insensible à la casse)
    List<Event> findByNameContainingIgnoreCase(String name);
    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Events dont la capacité n'est pas encore atteinte
    @Query("SELECT e FROM Event e WHERE e.currentParticipants < e.maxCapacity")
    List<Event> findAvailableEvents();
    @Query("SELECT e FROM Event e WHERE e.currentParticipants < e.maxCapacity")
    Page<Event> findAvailableEvents(Pageable pageable);

    // Events dans un intervalle de dates

    // Compter par statut
    @Query("SELECT e.status, COUNT(e) FROM Event e GROUP BY e.status")
    List<Object[]> countByStatus();

    // Compter par catégorie
    @Query("SELECT e.category.name, COUNT(e) FROM Event e GROUP BY e.category.name")
    List<Object[]> countByCategory();

    // Compter les events disponibles
    @Query("SELECT COUNT(e) FROM Event e WHERE e.currentParticipants < e.maxCapacity")
    long countAvailableEvents();

    @Query("SELECT e FROM Event e WHERE e.startDate BETWEEN :from AND :to")
    List<Event> findByDateRange(@Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to);

    @Query("SELECT e FROM Event e WHERE e.startDate BETWEEN :from AND :to")
    Page<Event> findByDateRange(@Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to,
                                Pageable pageable);

    @Query("""
        SELECT e FROM Event e
        WHERE e.id != :eventId
        AND (
            e.category.id = :categoryId
            OR e.location = :location
            OR (e.startDate BETWEEN :dateFrom AND :dateTo)
            OR (e.latitude IS NOT NULL AND e.longitude IS NOT NULL)
        )
        """)
    List<Event> findSimilarEvents(
            @Param("eventId") Long eventId,
            @Param("categoryId") Long categoryId,
            @Param("location") String location,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );



    List<Event> findByEndDateBefore(LocalDateTime now);

}