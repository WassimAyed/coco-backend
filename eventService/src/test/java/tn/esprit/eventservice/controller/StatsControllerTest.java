package tn.esprit.eventservice.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.eventservice.dto.StatsDTO;
import tn.esprit.eventservice.service.IStatsService;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StatsControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.context)
                .build();
    }

    @MockitoBean
    private IStatsService statsService;

    @Test
    @DisplayName("getGlobalStats — should return stats")
    void getGlobalStats_shouldReturnStats() throws Exception {
        StatsDTO stats = StatsDTO.builder()
                .totalEvents(100L)
                .totalParticipants(500L)
                .build();
        
        given(statsService.getGlobalStats()).willReturn(stats);

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").value(100))
                .andExpect(jsonPath("$.totalParticipants").value(500));
    }
}
