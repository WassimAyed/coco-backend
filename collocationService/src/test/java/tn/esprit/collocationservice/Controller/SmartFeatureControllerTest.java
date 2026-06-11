package tn.esprit.collocationservice.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.collocationservice.Service.SmartFeatureService;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SmartFeatureController Unit Tests")
class SmartFeatureControllerTest {

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleServerError(RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private SmartFeatureService smartFeatureService;
    @InjectMocks private SmartFeatureController controller;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("GET /collocation/smart/recommendations/{userId}")
    class GetRecommendationsTests {
        @Test
        @DisplayName("should return 200 with recommended offer IDs")
        void getRecommendations_shouldReturn200() throws Exception {
            when(smartFeatureService.getRecommendations(1L)).thenReturn(List.of(1L, 2L, 3L));

            mockMvc.perform(get("/collocation/smart/recommendations/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3));
        }

        @Test
        @DisplayName("should return 500 when service throws exception")
        void getRecommendations_whenServiceThrows_shouldReturn500() throws Exception {
            when(smartFeatureService.getRecommendations(anyLong()))
                    .thenThrow(new RuntimeException("Unexpected error"));

            mockMvc.perform(get("/collocation/smart/recommendations/1"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GET /collocation/smart/ranking/{offerId}")
    class GetApplicantRankingTests {
        @Test
        @DisplayName("should return 200 with ranking result")
        void getApplicantRanking_shouldReturn200() throws Exception {
            Map<String, Object> ranking = Map.of("ranked", List.of(200L));
            when(smartFeatureService.rankApplicants(1L)).thenReturn(ranking);

            mockMvc.perform(get("/collocation/smart/ranking/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 500 when service throws exception")
        void getApplicantRanking_whenServiceThrows_shouldReturn500() throws Exception {
            when(smartFeatureService.rankApplicants(anyLong()))
                    .thenThrow(new RuntimeException("AI error"));

            mockMvc.perform(get("/collocation/smart/ranking/1"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GET /collocation/smart/trust-score/{userId}")
    class GetTrustScoreTests {
        @Test
        @DisplayName("should return 200 with trust score")
        void getTrustScore_shouldReturn200() throws Exception {
            Map<String, Object> score = Map.of("score", 85.5);
            when(smartFeatureService.getTrustScore(1L)).thenReturn(score);

            mockMvc.perform(get("/collocation/smart/trust-score/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 500 when service throws exception")
        void getTrustScore_whenServiceThrows_shouldReturn500() throws Exception {
            when(smartFeatureService.getTrustScore(anyLong()))
                    .thenThrow(new RuntimeException("Internal error"));

            mockMvc.perform(get("/collocation/smart/trust-score/1"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("POST /collocation/smart/predict-price")
    class PredictPriceTests {
        @Test
        @DisplayName("should return 200 with prediction")
        void predictPrice_shouldReturn200() throws Exception {
            Map<String, Object> requestBody = Map.of("ville", "Tunis");
            when(smartFeatureService.predictPrice(any())).thenReturn(Map.of("price", 600));

            mockMvc.perform(post("/collocation/smart/predict-price")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 500 when service throws exception")
        void predictPrice_whenServiceThrows_shouldReturn500() throws Exception {
            when(smartFeatureService.predictPrice(any()))
                    .thenThrow(new RuntimeException("Predict error"));

            mockMvc.perform(post("/collocation/smart/predict-price")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isInternalServerError());
        }
    }
}
