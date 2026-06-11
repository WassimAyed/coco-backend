package tn.esprit.covoiturageservice.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
public class CovoiturageSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== Template fields (copied to each generated Covoiturage) =====
    @NotBlank(message = "Le point de depart est obligatoire.")
    @Size(max = 255, message = "Le point de depart ne peut pas depasser 255 caracteres.")
    private String pointDepart;

    @NotBlank(message = "Le point d'arrivee est obligatoire.")
    @Size(max = 255, message = "Le point d'arrivee ne peut pas depasser 255 caracteres.")
    private String pointArrivee;

    @Min(value = 1, message = "Le nombre de places doit etre au minimum 1.")
    @Max(value = 4, message = "Le nombre de places ne peut pas depasser 4.")
    private int nombrePlaces;

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

    // ===== Recurrence rule =====
    @NotNull(message = "La frequence est obligatoire.")
    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    // Comma-separated days for WEEKLY, e.g. "MON,WED,FRI"
    @Pattern(
        regexp = "^$|^(MON|TUE|WED|THU|FRI|SAT|SUN)(,(MON|TUE|WED|THU|FRI|SAT|SUN))*$",
        message = "Les jours doivent etre une liste valide (MON,TUE,WED,THU,FRI,SAT,SUN)."
    )
    private String daysOfWeek;

    @NotNull(message = "L'heure de depart est obligatoire.")
    private LocalTime heureDepart;

    private LocalDate startDate;
    private LocalDate endDate;

    private boolean active;

    // Last date a Covoiturage was generated for this schedule (avoids duplicates)
    private LocalDate lastGeneratedDate;

    private LocalDateTime createdAt;

    public enum Frequency { DAILY, WEEKLY }
}
