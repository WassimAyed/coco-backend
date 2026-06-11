package tn.esprit.covoiturageservice.Service;

import tn.esprit.covoiturageservice.Entity.Vehicule;

import java.util.List;

public interface IVehiculeService {
    Vehicule getVehiculeById(Long id);
    List<Vehicule> getAllVehicules();
    List<Vehicule> getVehiculesByUtilisateur(long idUtilisateur);
    Vehicule addVehicule(Vehicule vehicule);
    Vehicule updateVehicule(Vehicule vehicule);
    void deleteVehicule(Long id);
}