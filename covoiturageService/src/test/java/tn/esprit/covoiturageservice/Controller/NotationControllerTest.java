package tn.esprit.covoiturageservice.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.covoiturageservice.Entity.Notation;
import tn.esprit.covoiturageservice.Service.INotationService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotationController.class)
class NotationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private INotationService service;

    private final ObjectMapper json = new ObjectMapper();

    private Notation build() {
        Notation n = new Notation();
        n.setId(1L);
        n.setNotation(5);
        n.setComment("Tres bon trajet");
        n.setIdDonneur(10L);
        n.setIdRecepteur(20L);
        return n;
    }

    @Test
    void add_returnsOk() throws Exception {
        when(service.addNotation(any())).thenReturn(build());

        mockMvc.perform(post("/covoiturage/notation/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_returnsNotation() throws Exception {
        when(service.getNotationById(1L)).thenReturn(build());

        mockMvc.perform(get("/covoiturage/notation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAll_returnsList() throws Exception {
        when(service.getAllNotations()).thenReturn(List.of(build()));

        mockMvc.perform(get("/covoiturage/notation/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByDonneur_returnsList() throws Exception {
        when(service.getNotationsByDonneur(10L)).thenReturn(List.of(build()));

        mockMvc.perform(get("/covoiturage/notation/donneur/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByRecepteur_returnsList() throws Exception {
        when(service.getNotationsByRecepteur(20L)).thenReturn(List.of(build()));

        mockMvc.perform(get("/covoiturage/notation/recepteur/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void update_returnsOk() throws Exception {
        when(service.updateNotation(any())).thenReturn(build());

        mockMvc.perform(put("/covoiturage/notation/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(build())))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete("/covoiturage/notation/delete/1"))
                .andExpect(status().isOk());
    }
}
