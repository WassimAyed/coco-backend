package tn.esprit.covoiturageservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.covoiturageservice.Entity.Reservation;

import java.util.List;

public interface IReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByIdPassenger(Long idPassenger);
    List<Reservation> findByCovoiturageId(Long covoiturageId);
    List<Reservation> findByCovoiturageIdIn(List<Long> covoiturageIds);
}
