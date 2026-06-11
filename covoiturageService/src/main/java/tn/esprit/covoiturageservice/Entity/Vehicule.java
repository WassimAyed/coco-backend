package tn.esprit.covoiturageservice.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Vehicule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive(message = "Identifiant utilisateur invalide.")
    private long idUtilisateur;

    @NotBlank(message = "La marque est obligatoire.")
    @Size(min = 2, max = 50, message = "La marque doit contenir entre 2 et 50 caracteres.")
    private String marque;

    @NotBlank(message = "L'immatriculation est obligatoire.")
    @Size(min = 3, max = 20, message = "L'immatriculation doit contenir entre 3 et 20 caracteres.")
    @Pattern(
        regexp = "^[A-Za-z0-9 -]{3,20}$",
        message = "L'immatriculation contient des caracteres invalides."
    )
    private String immatriculation;

    @NotBlank(message = "La couleur est obligatoire.")
    @Size(min = 2, max = 30, message = "La couleur doit contenir entre 2 et 30 caracteres.")
    private String couleur;

    @Min(value = 1, message = "La capacite doit etre au minimum 1.")
    @Max(value = 7, message = "La capacite ne peut pas depasser 7.")
    private int capacite;

    private String image;
}
