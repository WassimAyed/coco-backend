package tn.esprit.covoiturageservice.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.covoiturageservice.Entity.Covoiturage;
import tn.esprit.covoiturageservice.Entity.Reservation;
import tn.esprit.covoiturageservice.Entity.StatusReservation;
import tn.esprit.covoiturageservice.Repository.ICovoiturageRepository;
import tn.esprit.covoiturageservice.Repository.IReservationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImp implements IReservationService {

    private final IReservationRepository reservationRepository;
    private final ICovoiturageRepository covoiturageRepository;

    @Override
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public List<Reservation> getReservationsByPassenger(Long idPassenger) {
        return reservationRepository.findByIdPassenger(idPassenger);
    }

    @Override
    public List<Reservation> getReservationsByCovoiturage(Long covoiturageId) {
        return reservationRepository.findByCovoiturageId(covoiturageId);
    }

    @Override
    public List<Reservation> getReservationsByDriver(Long idDriver) {
        // Recuperer tous les covoiturages publies par ce driver
        List<Covoiturage> mesTrajets = covoiturageRepository.findByIdDriver(idDriver);
        List<Long> covoiturageIds = mesTrajets.stream()
                .map(Covoiturage::getId)
                .collect(Collectors.toList());

        if (covoiturageIds.isEmpty()) {
            return List.of();
        }
        // Recuperer toutes les reservations de ces covoiturages
        return reservationRepository.findByCovoiturageIdIn(covoiturageIds);
    }

    @Override
    public Reservation addReservation(Reservation reservation) {
        reservation.setStatusReservation(StatusReservation.EN_ATTENTE);
        reservation.setDateReservation(java.time.LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation updateReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation accepterReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation non trouvee"));

        if (reservation.getStatusReservation() != StatusReservation.EN_ATTENTE) {
            throw new RuntimeException("Cette reservation a deja ete traitee");
        }

        Covoiturage covoiturage = covoiturageRepository.findById(reservation.getCovoiturageId())
                .orElseThrow(() -> new RuntimeException("Covoiturage non trouve"));

        if (covoiturage.getPlacesDisponibles() < reservation.getNbPassengers()) {
            throw new RuntimeException("Pas assez de places disponibles");
        }

        // Accepter et decremeneter les places
        reservation.setStatusReservation(StatusReservation.CONFIRMEE);
        covoiturage.setPlacesDisponibles(covoiturage.getPlacesDisponibles() - reservation.getNbPassengers());
        covoiturageRepository.save(covoiturage);

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation refuserReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation non trouvee"));

        if (reservation.getStatusReservation() != StatusReservation.EN_ATTENTE) {
            throw new RuntimeException("Cette reservation a deja ete traitee");
        }

        reservation.setStatusReservation(StatusReservation.REFUSEE);
        return reservationRepository.save(reservation);
    }

    @Override
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }
}
