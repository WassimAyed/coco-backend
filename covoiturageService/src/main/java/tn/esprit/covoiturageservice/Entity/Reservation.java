package tn.esprit.covoiturageservice.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull(message = "Le passager est obligatoire.")
    @Positive(message = "Identifiant passager invalide.")
    private Long idPassenger;

    private LocalDateTime dateReservation;

    @Min(value = 1, message = "Le nombre de passagers doit etre au minimum 1.")
    @Max(value = 4, message = "Le nombre de passagers ne peut pas depasser 4.")
    private int nbPassengers;

    @NotNull(message = "Le covoiturage est obligatoire.")
    @Positive(message = "Identifiant covoiturage invalide.")
    private Long covoiturageId;

    @Enumerated(EnumType.STRING)
    private StatusReservation statusReservation;
}
