package tn.esprit.collocationservice.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class collocOffre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 100, message = "Le titre doit contenir entre 5 et 100 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private Double prixLoc;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @NotNull(message = "Le nombre de chambres est obligatoire")
    @Min(value = 1, message = "Il doit y avoir au moins 1 chambre")
    private Integer chambres;

    @NotNull(message = "Le champ meublee est obligatoire")
    private Boolean meublee;

    @DecimalMin(value = "-90.0", message = "Latitude invalide")
    @DecimalMax(value = "90.0", message = "Latitude invalide")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude invalide")
    @DecimalMax(value = "180.0", message = "Longitude invalide")
    private Double longitude;

    @PastOrPresent(message = "La date de création doit être dans le passé ou aujourd'hui")
    private LocalDate createdAt;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @NotNull(message = "Le propriétaire est obligatoire")
    private Long ownerId;

    @Column(columnDefinition = "boolean default false")
    private Boolean notified = false;

    @OneToMany(mappedBy="offre", cascade=CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<collocOffreImage> imagesColoc;
}
