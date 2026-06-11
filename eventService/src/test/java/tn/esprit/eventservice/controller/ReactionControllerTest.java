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
import tn.esprit.eventservice.dto.ReactionDTO;
import tn.esprit.eventservice.dto.ReactionSummaryDTO;
import tn.esprit.eventservice.entity.ReactionType;
import tn.esprit.eventservice.service.IReactionService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReactionControllerTest {

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
    private IReactionService reactionService;

    @Test
    @DisplayName("addOrUpdate — should return 200 when valid")
    void addOrUpdate_shouldReturn200() throws Exception {
        // Given
        ReactionDTO dto = new ReactionDTO();
        dto.setEventId(1L);
        dto.setType(ReactionType.LIKE);
        dto.setAuthorName("John Doe");
        dto.setAuthorEmail("user@example.com");

        ReactionDTO responseDto = new ReactionDTO();
        responseDto.setId(10L);
        responseDto.setEventId(1L);
        responseDto.setType(ReactionType.LIKE);
        responseDto.setAuthorName("John Doe");
        responseDto.setAuthorEmail("user@example.com");

        given(reactionService.addOrUpdateReaction(any(ReactionDTO.class))).willReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.type").value("LIKE"));
    }

    @Test
    @DisplayName("remove — should return 204 when deleted")
    void remove_shouldReturn204() throws Exception {
        // Given
        doNothing().when(reactionService).removeReaction(1L, "user@example.com");

        // When & Then
        mockMvc.perform(delete("/api/reactions/event/1")
                .param("authorEmail", "user@example.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("getSummary — should return 200 with summary")
    void getSummary_shouldReturn200() throws Exception {
        // Given
        ReactionSummaryDTO summary = ReactionSummaryDTO.builder()
                .eventId(1L)
                .totalReactions(20L)
                .reactionCounts(java.util.Map.of(
                        ReactionType.LIKE, 5L,
                        ReactionType.LOVE, 10L
                ))
                .build();

        given(reactionService.getReactionSummary(1L)).willReturn(summary);

        // When & Then
        mockMvc.perform(get("/api/reactions/event/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reactionCounts.LIKE").value(5))
                .andExpect(jsonPath("$.reactionCounts.LOVE").value(10));
    }
}
