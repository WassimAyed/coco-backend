package tn.esprit.covoiturageservice.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.covoiturageservice.Entity.Vehicule;
import tn.esprit.covoiturageservice.Service.FileStorageService;
import tn.esprit.covoiturageservice.Service.IVehiculeService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehiculeController.class)
class VehiculeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IVehiculeService service;
    @MockitoBean
    private FileStorageService fileStorageService;

    private Vehicule build() {
        Vehicule v = new Vehicule();
        v.setId(1L);
        v.setIdUtilisateur(10L);
        v.setMarque("Peugeot");
        v.setImmatriculation("123 TU 4567");
        v.setCouleur("Noir");
        v.setCapacite(4);
        return v;
    }

    @Test
    void add_validParams_returnsOk() throws Exception {
        when(service.addVehicule(any())).thenReturn(build());

        mockMvc.perform(multipart("/covoiturage/vehicule/add")
                        .param("marque", "Peugeot")
                        .param("immatriculation", "123 TU 4567")
                        .param("couleur", "Noir")
                        .param("capacite", "4")
                        .param("idUtilisateur", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void add_blankMarque_returns400() throws Exception {
        mockMvc.perform(multipart("/covoiturage/vehicule/add")
                        .param("marque", "")
                        .param("immatriculation", "123 TU 4567")
                        .param("couleur", "Noir")
                        .param("capacite", "4")
                        .param("idUtilisateur", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_invalidImmat_returns400() throws Exception {
        mockMvc.perform(multipart("/covoiturage/vehicule/add")
                        .param("marque", "Peugeot")
                        .param("immatriculation", "@@@")
                        .param("couleur", "Noir")
                        .param("capacite", "4")
                        .param("idUtilisateur", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_capaciteTropGrande_returns400() throws Exception {
        mockMvc.perform(multipart("/covoiturage/vehicule/add")
                        .param("marque", "Peugeot")
                        .param("immatriculation", "123 TU 4567")
                        .param("couleur", "Noir")
                        .param("capacite", "20")
                        .param("idUtilisateur", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_returnsVehicule() throws Exception {
        when(service.getVehiculeById(1L)).thenReturn(build());

        mockMvc.perform(get("/covoiturage/vehicule/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAll_returnsList() throws Exception {
        when(service.getAllVehicules()).thenReturn(List.of(build()));

        mockMvc.perform(get("/covoiturage/vehicule/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByUtilisateur_returnsList() throws Exception {
        when(service.getVehiculesByUtilisateur(10L)).thenReturn(List.of(build()));

        mockMvc.perform(get("/covoiturage/vehicule/voitures/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void delete_returnsOk() throws Exception {
        when(service.getVehiculeById(1L)).thenReturn(build());

        mockMvc.perform(delete("/covoiturage/vehicule/delete/1"))
                .andExpect(status().isOk());
    }
}
