package tn.esprit.covoiturageservice.Repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import tn.esprit.covoiturageservice.Entity.Covoiturage;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
class ICovoiturageRepositoryTest {

    @Autowired
    private ICovoiturageRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("findByIdDriver renvoie les trajets d'un conducteur")
    void findByIdDriver_returnsTripsForDriver() {
        repository.save(buildTrip(10L, "Tunis", "Sousse", 4));
        repository.save(buildTrip(10L, "Sousse", "Sfax", 2));
        repository.save(buildTrip(20L, "Tunis", "Bizerte", 3));

        List<Covoiturage> result = repository.findByIdDriver(10L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("countDistinctDrivers compte les conducteurs uniques")
    void countDistinctDrivers_countsUnique() {
        repository.save(buildTrip(10L, "A", "B", 4));
        repository.save(buildTrip(10L, "A", "B", 4));
        repository.save(buildTrip(20L, "A", "B", 4));
        repository.save(buildTrip(30L, "A", "B", 4));

        assertThat(repository.countDistinctDrivers()).isEqualTo(3L);
    }

    @Test
    @DisplayName("sumPlacesDisponibles fait la somme des places")
    void sumPlacesDisponibles_sums() {
        repository.save(buildTrip(10L, "A", "B", 4));
        repository.save(buildTrip(20L, "A", "B", 3));
        repository.save(buildTrip(30L, "A", "B", 0));

        assertThat(repository.sumPlacesDisponibles()).isEqualTo(7L);
    }

    @Test
    @DisplayName("sumPlacesDisponibles renvoie 0 quand aucun trajet")
    void sumPlacesDisponibles_zeroWhenEmpty() {
        assertThat(repository.sumPlacesDisponibles()).isZero();
    }

    private Covoiturage buildTrip(Long idDriver, String depart, String arrivee, int placesDispo) {
        Covoiturage c = new Covoiturage();
        c.setIdDriver(idDriver);
        c.setVehicleId(1L);
        c.setPointDepart(depart);
        c.setPointArrivee(arrivee);
        c.setDateDepart(LocalDateTime.now().plusDays(1));
        c.setNombrePlaces(4);
        c.setPlacesDisponibles(placesDispo);
        c.setPrixParPassager(20.0);
        c.setDistance(100);
        c.setDureeEstimee(60);
        c.setLattitudeDepart(36.8);
        c.setLongitudeDepart(10.18);
        c.setLatitudeArrivee(35.83);
        c.setLongitudeArrivee(10.63);
        return c;
    }
}
