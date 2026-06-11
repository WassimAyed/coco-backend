package tn.esprit.covoiturageservice.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.covoiturageservice.Entity.CovoiturageSchedule;
import tn.esprit.covoiturageservice.Entity.CovoiturageSchedule.Frequency;
import tn.esprit.covoiturageservice.Service.ICovoiturageScheduleService;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CovoiturageScheduleController.class)
class CovoiturageScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICovoiturageScheduleService service;

    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());

    private CovoiturageSchedule valid() {
        CovoiturageSchedule s = new CovoiturageSchedule();
        s.setId(1L);
        s.setPointDepart("Tunis");
        s.setPointArrivee("Sousse");
        s.setNombrePlaces(4);
        s.setPrixParPassager(20.0);
        s.setIdDriver(10L);
        s.setVehicleId(5L);
        s.setHeureDepart(LocalTime.of(8, 0));
        s.setFrequency(Frequency.DAILY);
        s.setActive(true);
        return s;
    }

    @Test
    void add_validBody_returnsOk() throws Exception {
        when(service.add(any())).thenReturn(valid());

        mockMvc.perform(post("/covoiturage/schedule/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(valid())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void add_invalidBody_returns400() throws Exception {
        CovoiturageSchedule invalid = new CovoiturageSchedule();

        mockMvc.perform(post("/covoiturage/schedule/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_validBody_returnsOk() throws Exception {
        when(service.update(any())).thenReturn(valid());

        mockMvc.perform(put("/covoiturage/schedule/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(valid())))
                .andExpect(status().isOk());
    }

    @Test
    void getById_returnsSchedule() throws Exception {
        when(service.getById(1L)).thenReturn(valid());

        mockMvc.perform(get("/covoiturage/schedule/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_missing_returns404() throws Exception {
        when(service.getById(99L)).thenReturn(null);

        mockMvc.perform(get("/covoiturage/schedule/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_returnsList() throws Exception {
        when(service.getAll()).thenReturn(List.of(valid()));

        mockMvc.perform(get("/covoiturage/schedule/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByDriver_returnsList() throws Exception {
        when(service.getByDriver(10L)).thenReturn(List.of(valid()));

        mockMvc.perform(get("/covoiturage/schedule/driver/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete("/covoiturage/schedule/delete/1"))
                .andExpect(status().isOk());
    }

    @Test
    void toggle_returnsSchedule() throws Exception {
        CovoiturageSchedule toggled = valid();
        toggled.setActive(false);
        when(service.toggleActive(1L)).thenReturn(toggled);

        mockMvc.perform(patch("/covoiturage/schedule/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void toggle_missing_returns404() throws Exception {
        when(service.toggleActive(99L)).thenReturn(null);

        mockMvc.perform(patch("/covoiturage/schedule/99/toggle"))
                .andExpect(status().isNotFound());
    }

    @Test
    void runNow_returnsCount() throws Exception {
        when(service.generateDueCovoiturages()).thenReturn(3);

        mockMvc.perform(post("/covoiturage/schedule/run-now"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generated").value(3));
    }
}
