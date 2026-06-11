package tn.esprit.collocationservice.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Repository.UserActivityLogRepository;
import tn.esprit.collocationservice.Service.collocOffreRequestService;
import tn.esprit.collocationservice.Service.collocOffreService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollocationController Unit Tests")
class CollocationControllerTest {

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(Exception.class)
        public ResponseEntity<String> handleAll(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private collocOffreService collocationOffreService;
    @Mock private collocOffreRequestService collocOffreRequestService;
    @Mock private UserActivityLogRepository userActivityLogRepository;

    @InjectMocks
    private CollocationController controller;

    private collocOffre sampleOffer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Manually inject objectMapper if field injection is used in controller
        try {
            var field = CollocationController.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(controller, objectMapper);
        } catch (Exception e) {
            // ObjectMapper might not be present or named differently
        }

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        sampleOffer = new collocOffre();
        sampleOffer.setId(1L);
        sampleOffer.setTitre("Studio Tunis Centre");
        sampleOffer.setDescription("Magnifique studio au coeur de Tunis, proche de toutes commodités.");
        sampleOffer.setPrixLoc(500.0);
        sampleOffer.setVille("Tunis");
        sampleOffer.setChambres(2);
        sampleOffer.setMeublee(true);
        sampleOffer.setLatitude(36.8065);
        sampleOffer.setLongitude(10.1815);
        sampleOffer.setOwnerId(100L);
        sampleOffer.setCreatedAt(LocalDate.now());
    }

    @Test
    @DisplayName("GET /collocation/offresCollocGetAll")
    void getAllOffers_shouldReturn200() throws Exception {
        when(collocationOffreService.getAll()).thenReturn(List.of(sampleOffer));
        mockMvc.perform(get("/collocation/offresCollocGetAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titre").value("Studio Tunis Centre"));
    }

    @Test
    @DisplayName("GET /collocation/offresColloc/{id}")
    void getOfferById_shouldReturn200() throws Exception {
        when(collocationOffreService.getById(1L)).thenReturn(sampleOffer);
        mockMvc.perform(get("/collocation/offresColloc/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titre").value("Studio Tunis Centre"));
    }

    @Test
    @DisplayName("PUT /collocation/updateOffreColloc/{id}")
    void updateOffer_shouldReturn200() throws Exception {
        when(collocationOffreService.updateOffre(eq(1L), any())).thenReturn(sampleOffer);
        mockMvc.perform(put("/collocation/updateOffreColloc/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOffer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Studio Tunis Centre"));
    }

    @Test
    @DisplayName("DELETE /collocation/deleteOffreColloc/{id}")
    void deleteOffer_shouldReturn200() throws Exception {
        doNothing().when(collocationOffreService).deleteOffre(1L);
        mockMvc.perform(delete("/collocation/deleteOffreColloc/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Offre deleted successfully")));
    }

    @Test
    @DisplayName("POST /collocation/requests")
    void createRequest_shouldReturn200() throws Exception {
        Map<String, Object> input = Map.of("offerId", 1L);
        mockMvc.perform(post("/collocation/requests")
                        .header("X-USER-ID", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Request sent")));
    }

    @Test
    @DisplayName("POST /collocation/requests without offer ID")
    void createRequest_whenOfferIdIsMissing_shouldReturn500() throws Exception {
        mockMvc.perform(post("/collocation/requests")
                        .header("X-USER-ID", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isInternalServerError());

        verify(collocOffreRequestService, never()).create(any(), anyLong());
    }

    @Test
    @DisplayName("GET /collocation/offres/searchColloc")
    void searchOffers_shouldReturnPage() throws Exception {
        Page<collocOffre> page = new PageImpl<>(List.of(sampleOffer), PageRequest.of(0, 10), 1);
        when(collocationOffreService.search(any(), any())).thenReturn(page);

        mockMvc.perform(get("/collocation/offres/searchColloc")
                        .param("ville", "Tunis")
                        .param("minPrixLoc", "100")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titre").value("Studio Tunis Centre"));
    }

    @Test
    @DisplayName("POST /collocation/offresCollocCreate")
    void createOffer_shouldReturn200() throws Exception {
        String json = objectMapper.writeValueAsString(sampleOffer);
        MockMultipartFile offrePart = new MockMultipartFile("offre", "", "application/json", json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        
        mockMvc.perform(multipart("/collocation/offresCollocCreate")
                        .file(offrePart)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Offer created successfully")));
    }

    @Test
    @DisplayName("POST /collocation/offresCollocCreate with images")
    void createOffer_withImages_shouldReturn200() throws Exception {
        String json = objectMapper.writeValueAsString(sampleOffer);
        MockMultipartFile offrePart = new MockMultipartFile("offre", "", "application/json", json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        MockMultipartFile imagePart = new MockMultipartFile("imagesColloc", "image.jpg", "image/jpeg", "image data".getBytes());
        
        mockMvc.perform(multipart("/collocation/offresCollocCreate")
                        .file(offrePart)
                        .file(imagePart)
                        .param("userId", "1"))
                .andExpect(status().isOk());
        
        verify(collocationOffreService).create(any(), anyList(), eq(1L));
    }

    @Test
    @DisplayName("GET /collocation/offresColloc/{id} without userId")
    void getOfferById_withoutUserId_shouldReturn200AndNotLog() throws Exception {
        when(collocationOffreService.getById(1L)).thenReturn(sampleOffer);
        
        mockMvc.perform(get("/collocation/offresColloc/1"))
                .andExpect(status().isOk());
        
        verify(userActivityLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("GET /collocation/offresColloc/{id} with X-USER-ID")
    void getOfferById_withUserId_shouldLogActivityAndReturn200() throws Exception {
        when(collocationOffreService.getById(1L)).thenReturn(sampleOffer);
        
        mockMvc.perform(get("/collocation/offresColloc/1")
                        .header("X-USER-ID", "100"))
                .andExpect(status().isOk());
        
        verify(userActivityLogRepository).save(any());
    }

    @Test
    @DisplayName("GET /collocation/offresColloc/{id} keeps returning offer when activity logging fails")
    void getOfferById_whenActivityLoggingFails_shouldReturn200() throws Exception {
        when(collocationOffreService.getById(1L)).thenReturn(sampleOffer);
        when(userActivityLogRepository.save(any())).thenThrow(new RuntimeException("log failed"));

        mockMvc.perform(get("/collocation/offresColloc/1")
                        .header("X-USER-ID", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userActivityLogRepository).save(any());
    }

    @Test
    @DisplayName("GET /collocation/myOffresColloc/{ownerId}")
    void getMyOffers_shouldReturn200() throws Exception {
        when(collocationOffreService.findByOwnerId(100L)).thenReturn(List.of(sampleOffer));
        mockMvc.perform(get("/collocation/myOffresColloc/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /collocation/nearby")
    void getNearbyOffers_shouldReturn200() throws Exception {
        when(collocationOffreService.getNearbyOffers(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(sampleOffer));
        mockMvc.perform(get("/collocation/nearby")
                        .param("lat", "36.8")
                        .param("lng", "10.2")
                        .param("radius", "5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
