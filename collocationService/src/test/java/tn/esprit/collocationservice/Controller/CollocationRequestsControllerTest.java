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
import tn.esprit.collocationservice.Dto.CollocOffreRequestDTO;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreRequest;
import tn.esprit.collocationservice.Service.collocOffreRequestService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollocationRequestsController Unit Tests")
class CollocationRequestsControllerTest {

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleServerError(RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private collocOffreRequestService service;
    @InjectMocks
    private CollocationRequestsController controller;

    private collocOffre sampleOffer;
    private collocOffreRequest sampleRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        sampleOffer = new collocOffre();
        sampleOffer.setId(1L);
        sampleOffer.setOwnerId(100L);

        sampleRequest = new collocOffreRequest();
        sampleRequest.setId(10L);
        sampleRequest.setOffer(sampleOffer);
        sampleRequest.setStudentId(200L);
        sampleRequest.setStatus(collocOffreRequest.Status.ENCOURS);
        sampleRequest.setMessage("Je suis intéressé");
    }

    // =========================================================================
    // CREATE REQUEST
    // =========================================================================
    @Nested
    @DisplayName("POST /collocation/requests/create")
    class CreateRequestTests {

        @Test
        @DisplayName("should return 200 when request is valid")
        void createRequest_shouldReturn200() throws Exception {
            CollocOffreRequestDTO input = CollocOffreRequestDTO.builder()
                    .offerId(1L)
                    .build();

            when(service.create(any(collocOffreRequest.class), eq(42L))).thenReturn(sampleRequest);

            mockMvc.perform(post("/collocation/requests/create")
                    .header("X-USER-ID", "42")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Request sent"));
        }

        @Test
        @DisplayName("should return 400 when offer is null in request body")
        void createRequest_whenOfferIsNull_shouldReturn400() throws Exception {
            // offerId is null in DTO — controller throws IllegalArgumentException → 400
            CollocOffreRequestDTO input = new CollocOffreRequestDTO();

            mockMvc.perform(post("/collocation/requests/create")
                    .header("X-USER-ID", "42")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest());

            verify(service, never()).create(any(), any());
        }

        @Test
        @DisplayName("should return 400 when offer ID is null")
        void createRequest_whenOfferIdIsNull_shouldReturn400() throws Exception {
            CollocOffreRequestDTO input = CollocOffreRequestDTO.builder()
                    .offerId(null)
                    .build();

            mockMvc.perform(post("/collocation/requests/create")
                    .header("X-USER-ID", "42")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest());

            verify(service, never()).create(any(), any());
        }
    }

    // =========================================================================
    // GET MY REQUESTS
    // =========================================================================
    @Nested
    @DisplayName("GET /collocation/requests/my")
    class GetMyRequestsTests {

        @Test
        @DisplayName("should return student's requests")
        void getMyRequests_shouldReturnList() throws Exception {
            when(service.getRequestsByStudent(200L)).thenReturn(List.of(sampleRequest));

            mockMvc.perform(get("/collocation/requests/my").header("userId", "200"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(10));
        }

        @Test
        @DisplayName("should return empty list when student has no requests")
        void getMyRequests_whenNone_shouldReturnEmpty() throws Exception {
            when(service.getRequestsByStudent(999L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/collocation/requests/my").header("userId", "999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // =========================================================================
    // GET REQUESTS FOR OWNER
    // =========================================================================
    @Nested
    @DisplayName("GET /collocation/requests/forOwner")
    class GetRequestsForOwnerTests {

        @Test
        @DisplayName("should return requests for the owner's offers")
        void getRequestsForOwner_shouldReturnList() throws Exception {
            when(service.getRequestsByOfferOwner(100L)).thenReturn(List.of(sampleRequest));

            mockMvc.perform(get("/collocation/requests/forOwner").header("userId", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("should return empty list when owner has no incoming requests")
        void getRequestsForOwner_whenNone_shouldReturnEmpty() throws Exception {
            when(service.getRequestsByOfferOwner(999L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/collocation/requests/forOwner").header("userId", "999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // =========================================================================
    // UPDATE REQUEST STATUS
    // =========================================================================
    @Nested
    @DisplayName("PUT /collocation/requests/{id}/status")
    class UpdateStatusTests {

        @Test
        @DisplayName("should update status to ACCEPTEE")
        void updateStatus_toAccepted_shouldReturn200() throws Exception {
            sampleRequest.setStatus(collocOffreRequest.Status.ACCEPTEE);
            when(service.updateStatus(10L, collocOffreRequest.Status.ACCEPTEE)).thenReturn(sampleRequest);

            mockMvc.perform(put("/collocation/requests/10/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("status", "ACCEPTEE"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACCEPTEE"));
        }

        @Test
        @DisplayName("should update status to REJETEE")
        void updateStatus_toRejected_shouldReturn200() throws Exception {
            sampleRequest.setStatus(collocOffreRequest.Status.REJETEE);
            when(service.updateStatus(10L, collocOffreRequest.Status.REJETEE)).thenReturn(sampleRequest);

            mockMvc.perform(put("/collocation/requests/10/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("status", "REJETEE"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REJETEE"));
        }

        @Test
        @DisplayName("should return 400 when invalid status string is provided (IllegalArgumentException)")
        void updateStatus_withInvalidStatus_shouldReturn400() throws Exception {
            // valueOf() throws IllegalArgumentException → our handler maps it to 400
            mockMvc.perform(put("/collocation/requests/10/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("status", "INVALID_STATUS"))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when status is missing")
        void updateStatus_whenStatusIsMissing_shouldReturn400() throws Exception {
            mockMvc.perform(put("/collocation/requests/10/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of())))
                    .andExpect(status().isBadRequest());

            verify(service, never()).updateStatus(anyLong(), any());
        }
    }

    // =========================================================================
    // DELETE REQUEST
    // =========================================================================
    @Nested
    @DisplayName("DELETE /collocation/requests/{id}")
    class DeleteRequestTests {

        @Test
        @DisplayName("should return 200 when request deleted successfully")
        void deleteRequest_shouldReturn200() throws Exception {
            doNothing().when(service).deleteRequest(10L);

            mockMvc.perform(delete("/collocation/requests/10"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Request deleted"));
        }

        @Test
        @DisplayName("should return 500 when deletion fails")
        void deleteRequest_whenFails_shouldReturn500() throws Exception {
            // CollocationRequestsController catches exceptions internally and returns 500
            doThrow(new RuntimeException("DB error")).when(service).deleteRequest(999L);

            mockMvc.perform(delete("/collocation/requests/999"))
                    .andExpect(status().isInternalServerError());
        }
    }

    // =========================================================================
    // GET REQUESTS BY OFFER IDS
    // =========================================================================
    @Nested
    @DisplayName("POST /collocation/requests/byOfferIds")
    class GetRequestsByOfferIdsTests {

        @Test
        @DisplayName("should return filtered requests for given offer IDs and owner")
        void getRequestsByOfferIds_shouldReturnFiltered() throws Exception {
            when(service.getRequestsByOfferIds(List.of(1L, 2L), 100L))
                    .thenReturn(List.of(sampleRequest));

            mockMvc.perform(post("/collocation/requests/byOfferIds")
                    .header("userId", "100")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("should return empty list when no offers match")
        void getRequestsByOfferIds_whenNoMatch_shouldReturnEmpty() throws Exception {
            when(service.getRequestsByOfferIds(anyList(), anyLong())).thenReturn(Collections.emptyList());

            mockMvc.perform(post("/collocation/requests/byOfferIds")
                    .header("userId", "100")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(List.of(99L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
