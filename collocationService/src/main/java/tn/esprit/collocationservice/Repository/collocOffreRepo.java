package tn.esprit.collocationservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.collocationservice.Entity.collocOffre;

import java.time.LocalDate;
import java.util.List;

public interface collocOffreRepo extends JpaRepository<collocOffre, Long>,
        JpaSpecificationExecutor<collocOffre> {

    List<collocOffre> findByOwnerId(Long ownerId);

    @Query("SELECT c FROM collocOffre c WHERE c.expiryDate < :date AND (c.notified = false OR c.notified IS NULL)")
    List<collocOffre> findExpiredAndUnnotifiedOffers(@Param("date") LocalDate date);
    @Query(value = """
SELECT * FROM colloc_offre o
WHERE (
    6371 *
    acos(
        cos(radians(:lat)) *
        cos(radians(o.latitude)) *
        cos(radians(o.longitude) - radians(:lng)) +
        sin(radians(:lat)) *
        sin(radians(o.latitude))
    )
) <= :radius
""", nativeQuery = true)
    List<collocOffre> findNearbyOffers(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radius
    );

}
