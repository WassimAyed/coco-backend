package tn.esprit.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.eventservice.entity.EventGallery;
import java.util.List;

@Repository
public interface EventGalleryRepository extends JpaRepository<EventGallery, Long> {
    List<EventGallery> findByEventId(Long eventId);
}