package tn.esprit.eventservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.eventservice.dto.ParticipantDTO;
import tn.esprit.eventservice.service.IParticipantService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ParticipantControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.context)
                .build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IParticipantService participantService;

    @Test
    @DisplayName("register — should return 201 when payload is valid")
    void register_shouldReturn201() throws Exception {
        ParticipantDTO dto = ParticipantDTO.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .eventId(10L)
                .build();

        ParticipantDTO created = ParticipantDTO.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .eventId(10L)
                .build();

        given(participantService.registerParticipant(any(ParticipantDTO.class))).willReturn(created);

        mockMvc.perform(post("/api/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    @DisplayName("unregister — should return 204 when deleted")
    void unregister_shouldReturn204() throws Exception {
        doNothing().when(participantService).unregisterParticipant(1L);

        mockMvc.perform(delete("/api/participants/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("getById — should return 200 with participant")
    void getById_shouldReturn200() throws Exception {
        ParticipantDTO dto = ParticipantDTO.builder()
                .id(1L)
                .fullName("Jane Doe")
                .build();

        given(participantService.getParticipantById(1L)).willReturn(dto);

        mockMvc.perform(get("/api/participants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"));
    }

    @Test
    @DisplayName("getByEvent — should return 200 with list of participants")
    void getByEvent_shouldReturn200() throws Exception {
        ParticipantDTO dto = ParticipantDTO.builder().id(1L).fullName("Jane").build();

        given(participantService.getParticipantsByEvent(10L)).willReturn(List.of(dto));

        mockMvc.perform(get("/api/participants/event/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("countByEvent — should return 200 with count")
    void countByEvent_shouldReturn200() throws Exception {
        given(participantService.countParticipantsByEvent(10L)).willReturn(5L);

        mockMvc.perform(get("/api/participants/event/10/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }
}
