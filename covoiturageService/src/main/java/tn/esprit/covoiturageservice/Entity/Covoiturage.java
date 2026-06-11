package tn.esprit.covoiturageservice.Entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Covoiturage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le point de depart est obligatoire.")
    @Size(max = 255, message = "Le point de depart ne peut pas depasser 255 caracteres.")
    private String pointDepart;

    @NotBlank(message = "Le point d'arrivee est obligatoire.")
    @Size(max = 255, message = "Le point d'arrivee ne peut pas depasser 255 caracteres.")
    private String pointArrivee;

    @NotNull(message = "La date de depart est obligatoire.")
    @Future(message = "La date de depart doit etre dans le futur.")
    private LocalDateTime dateDepart;

    @Min(value = 1, message = "Le nombre de places doit etre au minimum 1.")
    @Max(value = 4, message = "Le nombre de places ne peut pas depasser 4.")
    private int nombrePlaces;

    @Min(value = 0, message = "Les places disponibles ne peuvent pas etre negatives.")
    private int placesDisponibles;

    @DecimalMin(value = "-90.0", message = "Latitude depart invalide.")
    @DecimalMax(value = "90.0", message = "Latitude depart invalide.")
    private double lattitudeDepart;

    @DecimalMin(value = "-180.0", message = "Longitude depart invalide.")
    @DecimalMax(value = "180.0", message = "Longitude depart invalide.")
    private double longitudeDepart;

    @DecimalMin(value = "-90.0", message = "Latitude arrivee invalide.")
    @DecimalMax(value = "90.0", message = "Latitude arrivee invalide.")
    private double latitudeArrivee;

    @DecimalMin(value = "-180.0", message = "Longitude arrivee invalide.")
    @DecimalMax(value = "180.0", message = "Longitude arrivee invalide.")
    private double longitudeArrivee;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le prix par passager ne peut pas etre negatif.")
    @DecimalMax(value = "1000.0", message = "Le prix par passager est trop eleve.")
    private double prixParPassager;

    private Double prixSuggereParAI;

    @DecimalMin(value = "0.0", message = "La distance ne peut pas etre negative.")
    private double distance;

    @Min(value = 0, message = "La duree estimee ne peut pas etre negative.")
    private int dureeEstimee;

    @NotNull(message = "Le conducteur est obligatoire.")
    @Positive(message = "Identifiant conducteur invalide.")
    private Long idDriver;

    @NotNull(message = "Le vehicule est obligatoire.")
    @Positive(message = "Identifiant vehicule invalide.")
    private Long vehicleId;
}
