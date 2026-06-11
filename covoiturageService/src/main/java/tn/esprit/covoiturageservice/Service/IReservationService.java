package tn.esprit.covoiturageservice.Service;

import tn.esprit.covoiturageservice.Entity.Reservation;

import java.util.List;

public interface IReservationService {
    Reservation getReservationById(Long id);
    List<Reservation> getAllReservations();
    List<Reservation> getReservationsByPassenger(Long idPassenger);
    List<Reservation> getReservationsByCovoiturage(Long covoiturageId);
    List<Reservation> getReservationsByDriver(Long idDriver);
    Reservation addReservation(Reservation reservation);
    Reservation updateReservation(Reservation reservation);
    Reservation accepterReservation(Long reservationId);
    Reservation refuserReservation(Long reservationId);
    void deleteReservation(Long id);
}
