package tn.esprit.covoiturageservice.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.covoiturageservice.Entity.Reservation;
import tn.esprit.covoiturageservice.Service.IReservationService;

import java.util.List;

@RestController
@RequestMapping("/covoiturage/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService reservationService;

    @PostMapping("/add")
    public Reservation addReservation(@Valid @RequestBody Reservation reservation) {
        return reservationService.addReservation(reservation);
    }

    @GetMapping("/{id}")
    public Reservation getReservationById(@PathVariable Long id) {
        return reservationService.getReservationById(id);
    }

    @GetMapping("/all")
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/mesReservation/{idPassenger}")
    public List<Reservation> getReservationsByPassenger(@PathVariable Long idPassenger) {
        return reservationService.getReservationsByPassenger(idPassenger);
    }

    @GetMapping("/covoiturage/{covoiturageId}")
    public List<Reservation> getReservationsByCovoiturage(@PathVariable Long covoiturageId) {
        return reservationService.getReservationsByCovoiturage(covoiturageId);
    }

    @GetMapping("/driver/{idDriver}")
    public List<Reservation> getReservationsByDriver(@PathVariable Long idDriver) {
        return reservationService.getReservationsByDriver(idDriver);
    }

    @PutMapping("/accepter/{id}")
    public Reservation accepterReservation(@PathVariable Long id) {
        return reservationService.accepterReservation(id);
    }

    @PutMapping("/refuser/{id}")
    public Reservation refuserReservation(@PathVariable Long id) {
        return reservationService.refuserReservation(id);
    }

    @PutMapping("/update")
    public Reservation updateReservation(@Valid @RequestBody Reservation reservation) {
        return reservationService.updateReservation(reservation);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
    }
}
