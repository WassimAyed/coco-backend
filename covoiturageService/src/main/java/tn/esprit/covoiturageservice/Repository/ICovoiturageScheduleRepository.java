package tn.esprit.covoiturageservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.covoiturageservice.Entity.CovoiturageSchedule;

import java.util.List;

public interface ICovoiturageScheduleRepository extends JpaRepository<CovoiturageSchedule, Long> {
    List<CovoiturageSchedule> findByIdDriver(Long idDriver);
    List<CovoiturageSchedule> findByActiveTrue();
}
