package tn.esprit.covoiturageservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.covoiturageservice.Entity.Vehicule;

import java.util.List;

public interface IVehiculeRepository extends JpaRepository<Vehicule, Long> {
    List<Vehicule> findByIdUtilisateur(long idUtilisateur);
}