package tn.esprit.covoiturageservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.covoiturageservice.Entity.Covoiturage;

import java.util.List;

public interface ICovoiturageRepository extends JpaRepository<Covoiturage, Long> {
    List<Covoiturage> findByIdDriver(Long idDriver);

    @Query("SELECT COUNT(DISTINCT c.idDriver) FROM Covoiturage c WHERE c.idDriver IS NOT NULL")
    long countDistinctDrivers();

    @Query("SELECT COALESCE(SUM(c.placesDisponibles), 0) FROM Covoiturage c")
    long sumPlacesDisponibles();
}
