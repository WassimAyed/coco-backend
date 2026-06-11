package tn.esprit.covoiturageservice.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.covoiturageservice.Dto.AdminStatsDTO;
import tn.esprit.covoiturageservice.Dto.CO2ImpactDTO;
import tn.esprit.covoiturageservice.Entity.Covoiturage;
import tn.esprit.covoiturageservice.Service.CO2CalculatorService;
import tn.esprit.covoiturageservice.Service.CovoiturageServiceImp;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CovoiturageController.class)
class CovoiturageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CovoiturageServiceImp service;
    @MockitoBean
    private CO2CalculatorService co2Service;

    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());

    private Covoiturage validTrip() {
        Covoiturage c = new Covoiturage();
        c.setId(1L);
        c.setPointDepart("Tunis");
        c.setPointArrivee("Sousse");
        c.setDateDepart(LocalDateTime.now().plusDays(1));
        c.setNombrePlaces(4);
        c.setPlacesDisponibles(3);
        c.setPrixParPassager(20.0);
        c.setIdDriver(10L);
        c.setVehicleId(5L);
        return c;
    }

    @Test
    void getById_returnsTrip() throws Exception {
        when(service.getCovoiturageById(1L)).thenReturn(validTrip());

        mockMvc.perform(get("/covoiturage/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAll_returnsList() throws Exception {
        when(service.getAllCovoiturage()).thenReturn(List.of(validTrip()));

        mockMvc.perform(get("/covoiturage/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByDriver_returnsList() throws Exception {
        when(service.getCovoiturageByIdDriver(10L)).thenReturn(List.of(validTrip()));

        mockMvc.perform(get("/covoiturage/driver/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void add_validBody_returnsCreated() throws Exception {
        Covoiturage trip = validTrip();
        when(service.addCovoiturage(any(Covoiturage.class))).thenReturn(trip);

        mockMvc.perform(post("/covoiturage/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(trip)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void add_invalidBody_returns400() throws Exception {
        Covoiturage invalid = new Covoiturage(); // tous les champs obligatoires sont absents

        mockMvc.perform(post("/covoiturage/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_validBody_returnsOk() throws Exception {
        Covoiturage trip = validTrip();
        when(service.updateCovoiturage(any(Covoiturage.class))).thenReturn(trip);

        mockMvc.perform(put("/covoiturage/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(trip)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete("/covoiturage/delete/1"))
                .andExpect(status().isOk());
    }

    @Test
    void co2Impact_existingTrip_returnsImpact() throws Exception {
        Covoiturage trip = validTrip();
        when(service.getCovoiturageById(1L)).thenReturn(trip);
        when(co2Service.compute(eq(trip), anyInt())).thenReturn(
                CO2ImpactDTO.builder().covoiturageId(1L).co2EconomiseTotalKg(5.0).build()
        );

        mockMvc.perform(get("/covoiturage/1/co2-impact"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.covoiturageId").value(1));
    }

    @Test
    void co2Impact_missingTrip_returns404() throws Exception {
        when(service.getCovoiturageById(99L)).thenReturn(null);

        mockMvc.perform(get("/covoiturage/99/co2-impact"))
                .andExpect(status().isNotFound());
    }

    @Test
    void similar_returnsList() throws Exception {
        when(service.getSimilarCovoiturages(eq(1L), anyInt())).thenReturn(List.of(validTrip()));

        mockMvc.perform(get("/covoiturage/1/similar?limit=4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void adminStats_returnsStats() throws Exception {
        when(service.getAdminStats()).thenReturn(new AdminStatsDTO(15L, 7L, 42L));

        mockMvc.perform(get("/covoiturage/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrajets").value(15))
                .andExpect(jsonPath("$.conducteursActifs").value(7))
                .andExpect(jsonPath("$.placesDisponibles").value(42));
    }
}
