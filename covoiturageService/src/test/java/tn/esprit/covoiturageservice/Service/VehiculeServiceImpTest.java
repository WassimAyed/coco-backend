package tn.esprit.covoiturageservice.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.covoiturageservice.Entity.Vehicule;
import tn.esprit.covoiturageservice.Repository.IVehiculeRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceImpTest {

    @Mock
    private IVehiculeRepository vehiculeRepository;

    @InjectMocks
    private VehiculeServiceImp service;

    private Vehicule vehicule;

    @BeforeEach
    void setUp() {
        vehicule = new Vehicule();
        vehicule.setId(1L);
        vehicule.setMarque("Peugeot");
        vehicule.setImmatriculation("123 TU 4567");
        vehicule.setCouleur("Noir");
        vehicule.setCapacite(4);
        vehicule.setIdUtilisateur(10L);
    }

    @Test
    @DisplayName("getVehiculeById renvoie le vehicule")
    void getVehiculeById_returns() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        assertThat(service.getVehiculeById(1L)).isEqualTo(vehicule);
    }

    @Test
    @DisplayName("getVehiculeById renvoie null si introuvable")
    void getVehiculeById_returnsNullWhenMissing() {
        when(vehiculeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.getVehiculeById(99L)).isNull();
    }

    @Test
    @DisplayName("getAllVehicules renvoie tous les vehicules")
    void getAllVehicules_returnsAll() {
        when(vehiculeRepository.findAll()).thenReturn(List.of(vehicule));
        assertThat(service.getAllVehicules()).hasSize(1);
    }

    @Test
    @DisplayName("getVehiculesByUtilisateur delegue au repository")
    void getVehiculesByUtilisateur_delegates() {
        when(vehiculeRepository.findByIdUtilisateur(10L)).thenReturn(List.of(vehicule));
        assertThat(service.getVehiculesByUtilisateur(10L)).containsExactly(vehicule);
    }

    @Test
    @DisplayName("addVehicule sauvegarde")
    void addVehicule_saves() {
        when(vehiculeRepository.save(vehicule)).thenReturn(vehicule);
        assertThat(service.addVehicule(vehicule)).isEqualTo(vehicule);
        verify(vehiculeRepository).save(vehicule);
    }

    @Test
    @DisplayName("updateVehicule sauvegarde")
    void updateVehicule_saves() {
        when(vehiculeRepository.save(vehicule)).thenReturn(vehicule);
        assertThat(service.updateVehicule(vehicule)).isEqualTo(vehicule);
    }

    @Test
    @DisplayName("deleteVehicule delegue au repository")
    void deleteVehicule_delegates() {
        service.deleteVehicule(1L);
        verify(vehiculeRepository).deleteById(1L);
    }
}
