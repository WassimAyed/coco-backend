package tn.esprit.collocationservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.collocationservice.Entity.collocOffreRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface collocOffreRequestRepo extends JpaRepository<collocOffreRequest, Long> {

    List<collocOffreRequest> findByStudentId(Long studentId);

    Optional<collocOffreRequest> findById(Long id);

    // New: filter requests by owner of the offer
    List<collocOffreRequest> findByOfferOwnerId(Long ownerId);

    List<collocOffreRequest> findByOfferId(Long offerId);

    long countByStudentIdAndStatus(Long studentId, collocOffreRequest.Status status);
    
    long countByStudentId(Long studentId);
}