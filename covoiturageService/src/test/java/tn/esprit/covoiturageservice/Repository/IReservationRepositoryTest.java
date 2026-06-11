package tn.esprit.covoiturageservice.Repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import tn.esprit.covoiturageservice.Entity.Reservation;
import tn.esprit.covoiturageservice.Entity.StatusReservation;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
class IReservationRepositoryTest {

    @Autowired
    private IReservationRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("findByIdPassenger renvoie les reservations d'un passager")
    void findByIdPassenger_returnsForPassenger() {
        repository.save(build(10L, 1L, 1));
        repository.save(build(10L, 2L, 1));
        repository.save(build(20L, 3L, 1));

        List<Reservation> result = repository.findByIdPassenger(10L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByCovoiturageId renvoie les reservations d'un trajet")
    void findByCovoiturageId_returnsForTrip() {
        repository.save(build(10L, 1L, 1));
        repository.save(build(20L, 1L, 2));
        repository.save(build(30L, 2L, 1));

        List<Reservation> result = repository.findByCovoiturageId(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByCovoiturageIdIn renvoie les reservations pour plusieurs trajets")
    void findByCovoiturageIdIn_multipleTrips() {
        repository.save(build(10L, 1L, 1));
        repository.save(build(10L, 2L, 1));
        repository.save(build(10L, 3L, 1));

        List<Reservation> result = repository.findByCovoiturageIdIn(List.of(1L, 2L));

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByCovoiturageIdIn renvoie liste vide pour ids inexistants")
    void findByCovoiturageIdIn_emptyForUnknown() {
        repository.save(build(10L, 1L, 1));
        assertThat(repository.findByCovoiturageIdIn(List.of(999L))).isEmpty();
    }

    private Reservation build(Long idPassenger, Long covoiturageId, int nbPassengers) {
        Reservation r = new Reservation();
        r.setIdPassenger(idPassenger);
        r.setCovoiturageId(covoiturageId);
        r.setNbPassengers(nbPassengers);
        r.setStatusReservation(StatusReservation.EN_ATTENTE);
        r.setDateReservation(LocalDateTime.now());
        return r;
    }
}
