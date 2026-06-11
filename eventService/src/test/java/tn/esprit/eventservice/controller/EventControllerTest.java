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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.eventservice.dto.EventDTO;
import tn.esprit.eventservice.entity.EventStatus;
import tn.esprit.eventservice.entity.EventType;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.service.AdminNotificationService;
import tn.esprit.eventservice.service.IEventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventControllerTest {

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
    private IEventService eventService;

    @MockitoBean
    private AdminNotificationService adminNotificationService;

    @Test
    @DisplayName("create — should return 201 when payload is valid")
    void create_shouldReturn201() throws Exception {
        // Given
        EventDTO dto = buildEventDto();
        EventDTO createdDto = buildEventDto();
        createdDto.setId(1L);

        given(eventService.createEvent(any(EventDTO.class))).willReturn(createdDto);
        doNothing().when(adminNotificationService).publishEventCreated(any(EventDTO.class));

        // When & Then
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Conference"));
    }

    @Test
    @DisplayName("getById — should return 200 with event when found")
    void getById_shouldReturn200_whenFound() throws Exception {
        // Given
        EventDTO dto = buildEventDto();
        dto.setId(1L);
        given(eventService.getEventById(1L)).willReturn(dto);

        // When & Then
        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Conference"));
    }

    @Test
    @DisplayName("getById — should return 404 when not found")
    void getById_shouldReturn404_whenNotFound() throws Exception {
        // Given
        given(eventService.getEventById(99L)).willThrow(new ResourceNotFoundException("Event not found"));

        // When & Then
        mockMvc.perform(get("/api/events/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getAll — should return 200 with page of events")
    void getAll_shouldReturn200() throws Exception {
        // Given
        EventDTO dto = buildEventDto();
        dto.setId(1L);
        Page<EventDTO> page = new PageImpl<>(List.of(dto));

        given(eventService.getAllEvents(any(Pageable.class))).willReturn(page);

        // When & Then
        mockMvc.perform(get("/api/events")
                .param("page", "0")
                .param("size", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Conference"));
    }

    @Test
    @DisplayName("delete — should return 204 when deleted")
    void delete_shouldReturn204() throws Exception {
        // Given
        doNothing().when(eventService).deleteEvent(1L);

        // When & Then
        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @DisplayName("updateStatus — should return 200 with updated event")
    void updateStatus_shouldReturn200() throws Exception {
        // Given
        EventDTO updatedDto = buildEventDto();
        updatedDto.setId(1L);
        updatedDto.setStatus(EventStatus.ACCEPTED);
        
        given(eventService.updateStatus(1L, EventStatus.ACCEPTED)).willReturn(updatedDto);

        // When & Then
        mockMvc.perform(patch("/api/events/1/status")
                .param("status", "ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    private EventDTO buildEventDto() {
        EventDTO dto = new EventDTO();
        dto.setName("Conference");
        dto.setLocation("Tunis");
        dto.setDescription("Tech event with long enough description");
        dto.setStartDate(LocalDateTime.now().plusDays(2));
        dto.setEndDate(LocalDateTime.now().plusDays(3));
        dto.setMaxCapacity(100);
        dto.setCategoryId(1L);
        dto.setStatus(EventStatus.PENDING);
        dto.setEventType(EventType.INDOOR);
        dto.setPrice(java.math.BigDecimal.valueOf(50));
        return dto;
    }
}
