package tn.esprit.covoiturageservice.Controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.covoiturageservice.Entity.Vehicule;
import tn.esprit.covoiturageservice.Service.FileStorageService;
import tn.esprit.covoiturageservice.Service.IVehiculeService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/covoiturage/vehicule")
@RequiredArgsConstructor
@Validated
public class VehiculeController {

    private final IVehiculeService vehiculeService;
    private final FileStorageService fileStorageService;

    @PostMapping(value = "/add", consumes = "multipart/form-data")
    public Vehicule addVehicule(
            @RequestParam("marque") @NotBlank(message = "La marque est obligatoire.") @Size(min = 2, max = 50, message = "La marque doit contenir entre 2 et 50 caracteres.") String marque,
            @RequestParam("immatriculation") @NotBlank(message = "L'immatriculation est obligatoire.") @Pattern(regexp = "^[A-Za-z0-9 -]{3,20}$", message = "L'immatriculation contient des caracteres invalides.") String immatriculation,
            @RequestParam("couleur") @NotBlank(message = "La couleur est obligatoire.") @Size(min = 2, max = 30, message = "La couleur doit contenir entre 2 et 30 caracteres.") String couleur,
            @RequestParam("capacite") @Min(value = 1, message = "La capacite doit etre au minimum 1.") @Max(value = 7, message = "La capacite ne peut pas depasser 7.") int capacite,
            @RequestParam("idUtilisateur") @Positive(message = "Identifiant utilisateur invalide.") long idUtilisateur,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        Vehicule vehicule = new Vehicule();
        vehicule.setMarque(marque);
        vehicule.setImmatriculation(immatriculation);
        vehicule.setCouleur(couleur);
        vehicule.setCapacite(capacite);
        vehicule.setIdUtilisateur(idUtilisateur);

        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = fileStorageService.store(imageFile);
            vehicule.setImage(filename);
        }

        return vehiculeService.addVehicule(vehicule);
    }

    @GetMapping("/{id}")
    public Vehicule getVehiculeById(@PathVariable Long id) {
        return vehiculeService.getVehiculeById(id);
    }

    @GetMapping("/all")
    public List<Vehicule> getAllVehicules() {
        return vehiculeService.getAllVehicules();
    }

    @GetMapping("/voitures/{idUtilisateur}")
    public List<Vehicule> getVehiculesByUtilisateur(@PathVariable long idUtilisateur) {
        return vehiculeService.getVehiculesByUtilisateur(idUtilisateur);
    }

    @PutMapping(value = "/update", consumes = "multipart/form-data")
    public Vehicule updateVehicule(
            @RequestParam("id") @Positive(message = "Identifiant vehicule invalide.") Long id,
            @RequestParam("marque") @NotBlank(message = "La marque est obligatoire.") @Size(min = 2, max = 50, message = "La marque doit contenir entre 2 et 50 caracteres.") String marque,
            @RequestParam("immatriculation") @NotBlank(message = "L'immatriculation est obligatoire.") @Pattern(regexp = "^[A-Za-z0-9 -]{3,20}$", message = "L'immatriculation contient des caracteres invalides.") String immatriculation,
            @RequestParam("couleur") @NotBlank(message = "La couleur est obligatoire.") @Size(min = 2, max = 30, message = "La couleur doit contenir entre 2 et 30 caracteres.") String couleur,
            @RequestParam("capacite") @Min(value = 1, message = "La capacite doit etre au minimum 1.") @Max(value = 7, message = "La capacite ne peut pas depasser 7.") int capacite,
            @RequestParam("idUtilisateur") @Positive(message = "Identifiant utilisateur invalide.") long idUtilisateur,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        Vehicule vehicule = vehiculeService.getVehiculeById(id);
        if (vehicule == null) {
            throw new RuntimeException("Vehicule non trouve");
        }

        vehicule.setMarque(marque);
        vehicule.setImmatriculation(immatriculation);
        vehicule.setCouleur(couleur);
        vehicule.setCapacite(capacite);
        vehicule.setIdUtilisateur(idUtilisateur);

        if (imageFile != null && !imageFile.isEmpty()) {
            // Supprimer l'ancienne image
            if (vehicule.getImage() != null && !vehicule.getImage().isEmpty()) {
                fileStorageService.delete(vehicule.getImage());
            }
            String filename = fileStorageService.store(imageFile);
            vehicule.setImage(filename);
        }

        return vehiculeService.updateVehicule(vehicule);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteVehicule(@PathVariable Long id) {
        Vehicule vehicule = vehiculeService.getVehiculeById(id);
        if (vehicule != null && vehicule.getImage() != null && !vehicule.getImage().isEmpty()) {
            fileStorageService.delete(vehicule.getImage());
        }
        vehiculeService.deleteVehicule(id);
    }
}
