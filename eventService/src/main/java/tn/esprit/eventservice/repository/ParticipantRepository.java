package tn.esprit.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.eventservice.entity.Participant;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    // Tous les participants d'un event
    List<Participant> findByEventId(Long eventId);

    // Vérifier si un email est déjà inscrit à un event
    boolean existsByEmailAndEventId(String email, Long eventId);

    // Trouver un participant par email
    Optional<Participant> findByEmail(String email);

    // Compter les participants d'un event
    long countByEventId(Long eventId);


    // Compter participants par event
    @Query("SELECT p.event.name, COUNT(p) FROM Participant p GROUP BY p.event.name")
    List<Object[]> countByEvent();

    @Query("SELECT DISTINCT p.event.id FROM Participant p WHERE LOWER(p.email) = LOWER(:email)")
    List<Long> findDistinctEventIdsByEmail(@Param("email") String email);

    @Query("SELECT DISTINCT p.event.id FROM Participant p WHERE p.phone = :phone")
    List<Long> findDistinctEventIdsByPhone(@Param("phone") String phone);
}