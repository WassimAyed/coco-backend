package tn.esprit.covoiturageservice.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.covoiturageservice.Entity.Reservation;
import tn.esprit.covoiturageservice.Entity.StatusReservation;
import tn.esprit.covoiturageservice.Service.IReservationService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReservationService service;

    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());

    private Reservation valid() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setIdPassenger(10L);
        r.setCovoiturageId(1L);
        r.setNbPassengers(2);
        r.setStatusReservation(StatusReservation.EN_ATTENTE);
        return r;
    }

    @Test
    void add_validBody_returnsOk() throws Exception {
        when(service.addReservation(any())).thenReturn(valid());

        mockMvc.perform(post("/covoiturage/reservation/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(valid())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void add_invalidBody_returns400() throws Exception {
        Reservation invalid = new Reservation();

        mockMvc.perform(post("/covoiturage/reservation/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_returnsReservation() throws Exception {
        when(service.getReservationById(1L)).thenReturn(valid());

        mockMvc.perform(get("/covoiturage/reservation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAll_returnsList() throws Exception {
        when(service.getAllReservations()).thenReturn(List.of(valid()));

        mockMvc.perform(get("/covoiturage/reservation/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByPassenger_returnsList() throws Exception {
        when(service.getReservationsByPassenger(10L)).thenReturn(List.of(valid()));

        mockMvc.perform(get("/covoiturage/reservation/mesReservation/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByCovoiturage_returnsList() throws Exception {
        when(service.getReservationsByCovoiturage(1L)).thenReturn(List.of(valid()));

        mockMvc.perform(get("/covoiturage/reservation/covoiturage/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByDriver_returnsList() throws Exception {
        when(service.getReservationsByDriver(20L)).thenReturn(List.of(valid()));

        mockMvc.perform(get("/covoiturage/reservation/driver/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void accepter_returnsUpdated() throws Exception {
        Reservation r = valid();
        r.setStatusReservation(StatusReservation.CONFIRMEE);
        when(service.accepterReservation(1L)).thenReturn(r);

        mockMvc.perform(put("/covoiturage/reservation/accepter/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusReservation").value("CONFIRMEE"));
    }

    @Test
    void refuser_returnsUpdated() throws Exception {
        Reservation r = valid();
        r.setStatusReservation(StatusReservation.REFUSEE);
        when(service.refuserReservation(1L)).thenReturn(r);

        mockMvc.perform(put("/covoiturage/reservation/refuser/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusReservation").value("REFUSEE"));
    }

    @Test
    void update_validBody_returnsOk() throws Exception {
        when(service.updateReservation(any())).thenReturn(valid());

        mockMvc.perform(put("/covoiturage/reservation/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(valid())))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete("/covoiturage/reservation/delete/1"))
                .andExpect(status().isOk());
    }
}
