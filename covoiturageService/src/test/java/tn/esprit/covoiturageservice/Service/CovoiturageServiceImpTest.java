package tn.esprit.covoiturageservice.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.covoiturageservice.Dto.AdminStatsDTO;
import tn.esprit.covoiturageservice.Entity.Covoiturage;
import tn.esprit.covoiturageservice.Entity.Reservation;
import tn.esprit.covoiturageservice.Repository.ICovoiturageRepository;
import tn.esprit.covoiturageservice.Repository.IReservationRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CovoiturageServiceImpTest {

    @Mock
    private ICovoiturageRepository covoiturageRepository;
    @Mock
    private IReservationRepository reservationRepository;

    @InjectMocks
    private CovoiturageServiceImp service;

    private Covoiturage trip;

    @BeforeEach
    void setUp() {
        trip = newTrip(1L, "Tunis", "Sousse", 3);
    }

    @Test
    @DisplayName("getCovoiturageById renvoie le trajet quand il existe")
    void getCovoiturageById_returnsTrip() {
        when(covoiturageRepository.findById(1L)).thenReturn(Optional.of(trip));
        assertThat(service.getCovoiturageById(1L)).isEqualTo(trip);
    }

    @Test
    @DisplayName("getCovoiturageById renvoie null quand introuvable")
    void getCovoiturageById_returnsNullWhenMissing() {
        when(covoiturageRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.getCovoiturageById(99L)).isNull();
    }

    @Test
    @DisplayName("getCovoiturageByIdDriver delegue au repository")
    void getCovoiturageByIdDriver_delegatesToRepo() {
        when(covoiturageRepository.findByIdDriver(10L)).thenReturn(List.of(trip));
        assertThat(service.getCovoiturageByIdDriver(10L)).containsExactly(trip);
    }

    @Test
    @DisplayName("getAllCovoiturage renvoie tous les trajets")
    void getAllCovoiturage_returnsAll() {
        when(covoiturageRepository.findAll()).thenReturn(List.of(trip));
        assertThat(service.getAllCovoiturage()).hasSize(1);
    }

    @Test
    @DisplayName("addCovoiturage sauvegarde le trajet")
    void addCovoiturage_savesTrip() {
        when(covoiturageRepository.save(trip)).thenReturn(trip);
        assertThat(service.addCovoiturage(trip)).isEqualTo(trip);
        verify(covoiturageRepository).save(trip);
    }

    @Test
    @DisplayName("updateCovoiturage sauvegarde le trajet")
    void updateCovoiturage_savesTrip() {
        when(covoiturageRepository.save(trip)).thenReturn(trip);
        assertThat(service.updateCovoiturage(trip)).isEqualTo(trip);
    }

    @Test
    @DisplayName("deleteCovoiturage supprime aussi les reservations liees")
    void deleteCovoiturage_alsoDeletesRelatedReservations() {
        Reservation r = new Reservation();
        r.setId(1L);
        when(reservationRepository.findByCovoiturageId(1L)).thenReturn(List.of(r));

        service.deleteCovoiturage(1L);

        verify(reservationRepository).deleteAll(List.of(r));
        verify(covoiturageRepository).deleteById(1L);
    }

    @Test
    @DisplayName("getSimilarCovoiturages renvoie liste vide si trajet inexistant")
    void getSimilarCovoiturages_emptyWhenMissing() {
        when(covoiturageRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.getSimilarCovoiturages(99L, 4)).isEmpty();
    }

    @Test
    @DisplayName("getSimilarCovoiturages priorise les trajets exacts")
    void getSimilarCovoiturages_prefersExactMatches() {
        Covoiturage exact = newTrip(2L, "Tunis", "Sousse", 1);
        Covoiturage partial = newTrip(3L, "Tunis", "Monastir", 2);
        Covoiturage unrelated = newTrip(4L, "Bizerte", "Gabes", 1);

        when(covoiturageRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(covoiturageRepository.findAll()).thenReturn(Arrays.asList(trip, exact, partial, unrelated));

        List<Covoiturage> result = service.getSimilarCovoiturages(1L, 4);

        assertThat(result).extracting(Covoiturage::getId).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("getSimilarCovoiturages exclut les trajets passes ou complets")
    void getSimilarCovoiturages_excludesPastOrFull() {
        Covoiturage past = newTrip(2L, "Tunis", "Sousse", 2);
        past.setDateDepart(LocalDateTime.now().minusDays(1));
        Covoiturage full = newTrip(3L, "Tunis", "Sousse", 0);

        when(covoiturageRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(covoiturageRepository.findAll()).thenReturn(Arrays.asList(trip, past, full));

        assertThat(service.getSimilarCovoiturages(1L, 4)).isEmpty();
    }

    @Test
    @DisplayName("getSimilarCovoiturages respecte la limite")
    void getSimilarCovoiturages_respectsLimit() {
        when(covoiturageRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(covoiturageRepository.findAll()).thenReturn(Arrays.asList(
                trip,
                newTrip(2L, "Tunis", "Sousse", 1),
                newTrip(3L, "Tunis", "Sousse", 1),
                newTrip(4L, "Tunis", "Sousse", 1)
        ));
        assertThat(service.getSimilarCovoiturages(1L, 2)).hasSize(2);
    }

    @Test
    @DisplayName("getAdminStats agrege les compteurs JPQL")
    void getAdminStats_aggregatesCounters() {
        when(covoiturageRepository.count()).thenReturn(15L);
        when(covoiturageRepository.countDistinctDrivers()).thenReturn(7L);
        when(covoiturageRepository.sumPlacesDisponibles()).thenReturn(42L);

        AdminStatsDTO stats = service.getAdminStats();

        assertThat(stats.getTotalTrajets()).isEqualTo(15L);
        assertThat(stats.getConducteursActifs()).isEqualTo(7L);
        assertThat(stats.getPlacesDisponibles()).isEqualTo(42L);
    }

    private Covoiturage newTrip(Long id, String depart, String arrivee, int placesDispo) {
        Covoiturage c = new Covoiturage();
        c.setId(id);
        c.setPointDepart(depart);
        c.setPointArrivee(arrivee);
        c.setDateDepart(LocalDateTime.now().plusDays(1));
        c.setNombrePlaces(4);
        c.setPlacesDisponibles(placesDispo);
        c.setPrixParPassager(20.0);
        c.setIdDriver(10L);
        c.setVehicleId(5L);
        return c;
    }
}
