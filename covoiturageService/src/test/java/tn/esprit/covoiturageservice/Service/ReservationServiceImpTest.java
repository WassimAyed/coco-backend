package tn.esprit.covoiturageservice.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.covoiturageservice.Entity.Covoiturage;
import tn.esprit.covoiturageservice.Entity.Reservation;
import tn.esprit.covoiturageservice.Entity.StatusReservation;
import tn.esprit.covoiturageservice.Repository.ICovoiturageRepository;
import tn.esprit.covoiturageservice.Repository.IReservationRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImpTest {

    @Mock
    private IReservationRepository reservationRepository;
    @Mock
    private ICovoiturageRepository covoiturageRepository;

    @InjectMocks
    private ReservationServiceImp service;

    private Reservation reservation;
    private Covoiturage trip;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setIdPassenger(10L);
        reservation.setCovoiturageId(1L);
        reservation.setNbPassengers(2);
        reservation.setStatusReservation(StatusReservation.EN_ATTENTE);

        trip = new Covoiturage();
        trip.setId(1L);
        trip.setNombrePlaces(4);
        trip.setPlacesDisponibles(3);
        trip.setIdDriver(20L);
    }

    @Test
    @DisplayName("getReservationById renvoie la reservation")
    void getReservationById_returns() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        assertThat(service.getReservationById(1L)).isEqualTo(reservation);
    }

    @Test
    @DisplayName("getAllReservations renvoie toutes les reservations")
    void getAllReservations_returnsAll() {
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));
        assertThat(service.getAllReservations()).hasSize(1);
    }

    @Test
    @DisplayName("getReservationsByPassenger delegue au repository")
    void getReservationsByPassenger_delegates() {
        when(reservationRepository.findByIdPassenger(10L)).thenReturn(List.of(reservation));
        assertThat(service.getReservationsByPassenger(10L)).containsExactly(reservation);
    }

    @Test
    @DisplayName("getReservationsByCovoiturage delegue au repository")
    void getReservationsByCovoiturage_delegates() {
        when(reservationRepository.findByCovoiturageId(1L)).thenReturn(List.of(reservation));
        assertThat(service.getReservationsByCovoiturage(1L)).containsExactly(reservation);
    }

    @Test
    @DisplayName("getReservationsByDriver renvoie liste vide quand aucun trajet")
    void getReservationsByDriver_emptyWhenNoTrips() {
        when(covoiturageRepository.findByIdDriver(20L)).thenReturn(List.of());
        assertThat(service.getReservationsByDriver(20L)).isEmpty();
    }

    @Test
    @DisplayName("getReservationsByDriver renvoie les reservations des trajets du driver")
    void getReservationsByDriver_returnsReservationsOfDriverTrips() {
        when(covoiturageRepository.findByIdDriver(20L)).thenReturn(List.of(trip));
        when(reservationRepository.findByCovoiturageIdIn(List.of(1L))).thenReturn(List.of(reservation));

        assertThat(service.getReservationsByDriver(20L)).containsExactly(reservation);
    }

    @Test
    @DisplayName("addReservation initialise statut et date avant sauvegarde")
    void addReservation_setsDefaults() {
        Reservation toAdd = new Reservation();
        toAdd.setIdPassenger(10L);
        toAdd.setCovoiturageId(1L);
        toAdd.setNbPassengers(1);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Reservation saved = service.addReservation(toAdd);

        assertThat(saved.getStatusReservation()).isEqualTo(StatusReservation.EN_ATTENTE);
        assertThat(saved.getDateReservation()).isNotNull();
    }

    @Test
    @DisplayName("updateReservation delegue au repository")
    void updateReservation_delegates() {
        when(reservationRepository.save(reservation)).thenReturn(reservation);
        assertThat(service.updateReservation(reservation)).isEqualTo(reservation);
    }

    @Test
    @DisplayName("accepterReservation passe a CONFIRMEE et decremente les places")
    void accepterReservation_confirmsAndDecrementsSeats() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(covoiturageRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        Reservation result = service.accepterReservation(1L);

        assertThat(result.getStatusReservation()).isEqualTo(StatusReservation.CONFIRMEE);
        assertThat(trip.getPlacesDisponibles()).isEqualTo(1);
        verify(covoiturageRepository).save(trip);
    }

    @Test
    @DisplayName("accepterReservation echoue si reservation introuvable")
    void accepterReservation_failsWhenNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.accepterReservation(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("non trouvee");
    }

    @Test
    @DisplayName("accepterReservation echoue si deja traitee")
    void accepterReservation_failsWhenAlreadyProcessed() {
        reservation.setStatusReservation(StatusReservation.CONFIRMEE);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.accepterReservation(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("deja ete traitee");
    }

    @Test
    @DisplayName("accepterReservation echoue si pas assez de places")
    void accepterReservation_failsWhenNotEnoughSeats() {
        trip.setPlacesDisponibles(1);
        reservation.setNbPassengers(2);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(covoiturageRepository.findById(1L)).thenReturn(Optional.of(trip));

        assertThatThrownBy(() -> service.accepterReservation(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pas assez de places");
    }

    @Test
    @DisplayName("refuserReservation passe a REFUSEE")
    void refuserReservation_setsRefused() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        Reservation result = service.refuserReservation(1L);

        assertThat(result.getStatusReservation()).isEqualTo(StatusReservation.REFUSEE);
    }

    @Test
    @DisplayName("refuserReservation echoue si deja traitee")
    void refuserReservation_failsWhenAlreadyProcessed() {
        reservation.setStatusReservation(StatusReservation.REFUSEE);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.refuserReservation(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deleteReservation delegue au repository")
    void deleteReservation_delegates() {
        service.deleteReservation(1L);
        verify(reservationRepository).deleteById(1L);
    }
}
