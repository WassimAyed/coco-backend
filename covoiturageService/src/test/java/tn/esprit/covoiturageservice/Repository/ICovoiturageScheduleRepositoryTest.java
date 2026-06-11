package tn.esprit.covoiturageservice.Repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import tn.esprit.covoiturageservice.Entity.CovoiturageSchedule;
import tn.esprit.covoiturageservice.Entity.CovoiturageSchedule.Frequency;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
class ICovoiturageScheduleRepositoryTest {

    @Autowired
    private ICovoiturageScheduleRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("findByIdDriver renvoie les schedules d'un conducteur")
    void findByIdDriver_returnsForDriver() {
        repository.save(build(10L, true));
        repository.save(build(10L, false));
        repository.save(build(20L, true));

        List<CovoiturageSchedule> result = repository.findByIdDriver(10L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByActiveTrue ne renvoie que les schedules actifs")
    void findByActiveTrue_returnsOnlyActive() {
        repository.save(build(10L, true));
        repository.save(build(20L, false));
        repository.save(build(30L, true));

        List<CovoiturageSchedule> result = repository.findByActiveTrue();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(CovoiturageSchedule::isActive);
    }

    private CovoiturageSchedule build(Long idDriver, boolean active) {
        CovoiturageSchedule s = new CovoiturageSchedule();
        s.setIdDriver(idDriver);
        s.setVehicleId(1L);
        s.setPointDepart("Tunis");
        s.setPointArrivee("Sousse");
        s.setNombrePlaces(4);
        s.setPrixParPassager(20.0);
        s.setHeureDepart(LocalTime.of(8, 0));
        s.setFrequency(Frequency.DAILY);
        s.setActive(active);
        return s;
    }
}
