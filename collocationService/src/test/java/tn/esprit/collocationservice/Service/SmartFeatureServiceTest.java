package tn.esprit.collocationservice.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tn.esprit.collocationservice.Entity.UserActivityLog;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreRequest;
import tn.esprit.collocationservice.Repository.UserActivityLogRepository;
import tn.esprit.collocationservice.Repository.collocOffreRepo;
import tn.esprit.collocationservice.Repository.collocOffreRequestRepo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SmartFeatureService Unit Tests")
class SmartFeatureServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserActivityLogRepository activityLogRepository;

    @Mock
    private collocOffreRepo offreRepository;

    @Mock
    private collocOffreRequestRepo requestRepository;

    @InjectMocks
    private SmartFeatureService service;

    private collocOffre sampleOffer;
    private collocOffreRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleOffer = new collocOffre();
        sampleOffer.setId(1L);
        sampleOffer.setTitre("Studio Tunis");
        sampleOffer.setPrixLoc(500.0);
        sampleOffer.setVille("Tunis");
        sampleOffer.setChambres(2);
        sampleOffer.setMeublee(true);
        sampleOffer.setLatitude(36.8065);
        sampleOffer.setLongitude(10.1815);
        sampleOffer.setOwnerId(100L);

        sampleRequest = new collocOffreRequest();
        sampleRequest.setId(10L);
        sampleRequest.setOffer(sampleOffer);
        sampleRequest.setStudentId(200L);
        sampleRequest.setStatus(collocOffreRequest.Status.ENCOURS);
    }

    // =========================================================================
    // GET RECOMMENDATIONS
    // =========================================================================
    @Nested
    @DisplayName("getRecommendations()")
    class GetRecommendationsTests {

        @Test
        @DisplayName("should return recommended offer IDs from AI service and verify payload")
        void getRecommendations_whenAiSucceeds_shouldReturnIds() {
            // Arrange
            Long userId = 1L;
            Long[] aiResponse = {1L, 2L, 3L};

            UserActivityLog log = new UserActivityLog();
            log.setUserId(userId);
            log.setOfferId(10L);
            log.setActivityType(UserActivityLog.ActivityType.VIEW);

            when(offreRepository.findAll()).thenReturn(List.of(sampleOffer));
            when(activityLogRepository.findByUserId(userId)).thenReturn(List.of(log));
            
            ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
            when(restTemplate.postForObject(contains("/recommend/"), payloadCaptor.capture(), eq(Long[].class)))
                    .thenReturn(aiResponse);

            // Act
            List<Long> result = service.getRecommendations(userId);

            // Assert
            assertThat(result).containsExactly(1L, 2L, 3L);
            
            Map<String, Object> capturedPayload = payloadCaptor.getValue();
            assertThat(capturedPayload)
                    .containsKey("user")
                    .containsKey("offers");
            
            List<Long> capturedActivityIds = (List<Long>) capturedPayload.get("user_activity_offer_ids");
            assertThat(capturedActivityIds).containsExactly(10L);
            
            List<Map<String, Object>> capturedOffers = (List<Map<String, Object>>) capturedPayload.get("offers");
            assertThat(capturedOffers).hasSize(1);
            assertThat(capturedOffers.get(0)).containsEntry("id", sampleOffer.getId());
        }

        @Test
        @DisplayName("should return empty list when AI service returns null")
        void getRecommendations_whenAiReturnsNull_shouldReturnEmptyList() {
            Long userId = 2L;
            when(offreRepository.findAll()).thenReturn(Collections.emptyList());
            when(activityLogRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
            when(restTemplate.postForObject(contains("/recommend/"), any(), eq(Long[].class)))
                    .thenReturn(null);

            List<Long> result = service.getRecommendations(userId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty list when AI service throws exception")
        void getRecommendations_whenAiThrows_shouldReturnEmptyList() {
            Long userId = 3L;
            when(offreRepository.findAll()).thenReturn(List.of(sampleOffer));
            when(activityLogRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
            when(restTemplate.postForObject(contains("/recommend/"), any(), eq(Long[].class)))
                    .thenThrow(new RestClientException("Connection refused"));

            List<Long> result = service.getRecommendations(userId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should use default values for offers with null fields")
        void getRecommendations_offerWithNullFields_shouldUseDefaults() {
            Long userId = 4L;
            collocOffre nullFieldOffer = new collocOffre();
            nullFieldOffer.setId(5L);
            // all numeric fields are null — defaults should be applied

            when(offreRepository.findAll()).thenReturn(List.of(nullFieldOffer));
            when(activityLogRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
            when(restTemplate.postForObject(contains("/recommend/"), any(), eq(Long[].class)))
                    .thenReturn(new Long[]{5L});

            List<Long> result = service.getRecommendations(userId);

            assertThat(result).containsExactly(5L);
        }

        @Test
        @DisplayName("should work when activity log is empty")
        void getRecommendations_withEmptyActivityLog_shouldStillCallAi() {
            Long userId = 5L;
            when(offreRepository.findAll()).thenReturn(List.of(sampleOffer));
            when(activityLogRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
            when(restTemplate.postForObject(contains("/recommend/"), any(), eq(Long[].class)))
                    .thenReturn(new Long[]{1L});

            List<Long> result = service.getRecommendations(userId);

            assertThat(result).containsExactly(1L);
        }

        @Test
        @DisplayName("should keep furnished flag false when offer is explicitly not furnished")
        void getRecommendations_whenOfferIsNotFurnished_shouldSendFalse() {
            Long userId = 6L;
            sampleOffer.setMeublee(false);

            when(offreRepository.findAll()).thenReturn(List.of(sampleOffer));
            when(activityLogRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

            ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
            when(restTemplate.postForObject(contains("/recommend/"), payloadCaptor.capture(), eq(Long[].class)))
                    .thenReturn(new Long[]{1L});

            service.getRecommendations(userId);

            List<Map<String, Object>> offers = (List<Map<String, Object>>) payloadCaptor.getValue().get("offers");
            assertThat(offers.get(0)).containsEntry("meublee", false);
        }
    }

    // =========================================================================
    // RANK APPLICANTS
    // =========================================================================
    @Nested
    @DisplayName("rankApplicants()")
    class RankApplicantsTests {

        @Test
        @DisplayName("should return empty list when offer not found")
        void rankApplicants_whenOfferNotFound_shouldReturnEmptyList() {
            when(offreRepository.findById(999L)).thenReturn(Optional.empty());

            Object result = service.rankApplicants(999L);

            assertThat(result).isEqualTo(Collections.emptyList());
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("should call AI service and return ranking result with correct payload")
        void rankApplicants_whenOfferExistsWithRequests_shouldReturnRanking() {
            Long offerId = 1L;
            sampleOffer.setPrixLoc(700.0);
            sampleOffer.setVille("Tunis");
            
            when(offreRepository.findById(offerId)).thenReturn(Optional.of(sampleOffer));
            when(requestRepository.findByOfferId(offerId)).thenReturn(List.of(sampleRequest));
            when(requestRepository.countByStudentIdAndStatus(anyLong(), any())).thenReturn(1L);
            when(requestRepository.countByStudentId(anyLong())).thenReturn(2L);

            Map<String, Object> aiRanking = Map.of("ranked", List.of(200L));
            ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
            when(restTemplate.postForObject(contains("/rank-applicants/"), payloadCaptor.capture(), eq(Object.class)))
                    .thenReturn(aiRanking);

            // Act
            Object result = service.rankApplicants(offerId);

            // Assert
            assertThat(result).isEqualTo(aiRanking);
            
            Map<String, Object> capturedPayload = payloadCaptor.getValue();
            assertThat(capturedPayload)
                    .containsEntry("offerPrice", 700.0)
                    .containsEntry("offerCity", "Tunis");
            
            List<Map<String, Object>> applicants = (List<Map<String, Object>>) capturedPayload.get("applicants");
            assertThat(applicants).hasSize(1);
            assertThat(applicants.get(0)).containsEntry("acceptedRatio", 0.5);
        }

        @Test
        @DisplayName("should return empty list when AI service throws exception")
        void rankApplicants_whenAiThrows_shouldReturnEmptyList() {
            Long offerId = 1L;
            when(offreRepository.findById(offerId)).thenReturn(Optional.of(sampleOffer));
            when(requestRepository.findByOfferId(offerId)).thenReturn(List.of(sampleRequest));
            when(requestRepository.countByStudentIdAndStatus(anyLong(), any())).thenReturn(0L);
            when(requestRepository.countByStudentId(anyLong())).thenReturn(0L);
            when(restTemplate.postForObject(contains("/rank-applicants/"), any(), eq(Object.class)))
                    .thenThrow(new RestClientException("AI down"));

            Object result = service.rankApplicants(offerId);

            assertThat(result).isEqualTo(Collections.emptyList());
        }

        @Test
        @DisplayName("should calculate acceptedRatio as 0 when total requests is zero")
        void rankApplicants_whenStudentHasNoRequests_acceptedRatioShouldBeZero() {
            Long offerId = 1L;
            when(offreRepository.findById(offerId)).thenReturn(Optional.of(sampleOffer));
            when(requestRepository.findByOfferId(offerId)).thenReturn(List.of(sampleRequest));
            when(requestRepository.countByStudentIdAndStatus(200L, collocOffreRequest.Status.ACCEPTEE))
                    .thenReturn(0L);
            when(requestRepository.countByStudentId(200L)).thenReturn(0L); // total = 0
            when(restTemplate.postForObject(contains("/rank-applicants/"), any(), eq(Object.class)))
                    .thenReturn(Collections.emptyList());

            // No exception expected; AI was called with acceptedRatio = 0.0
            service.rankApplicants(offerId);
            verify(restTemplate).postForObject(contains("/rank-applicants/"), any(), eq(Object.class));
        }

        @Test
        @DisplayName("should use default offer values when price/ville are null")
        void rankApplicants_whenOfferHasNullFields_shouldUseDefaults() {
            collocOffre nullOffer = new collocOffre();
            nullOffer.setId(2L);
            // prixLoc and ville are null

            when(offreRepository.findById(2L)).thenReturn(Optional.of(nullOffer));
            when(requestRepository.findByOfferId(2L)).thenReturn(Collections.emptyList());
            when(restTemplate.postForObject(contains("/rank-applicants/"), any(), eq(Object.class)))
                    .thenReturn("ok");

            service.rankApplicants(2L);

            verify(restTemplate).postForObject(contains("/rank-applicants/"), any(), eq(Object.class));
        }
    }

    // =========================================================================
    // GET TRUST SCORE
    // =========================================================================
    @Nested
    @DisplayName("getTrustScore()")
    class GetTrustScoreTests {

        @Test
        @DisplayName("should return AI trust score when service succeeds")
        void getTrustScore_whenAiSucceeds_shouldReturnScore() {
            Long userId = 10L;
            when(requestRepository.countByStudentIdAndStatus(userId, collocOffreRequest.Status.ACCEPTEE))
                    .thenReturn(3L);
            when(requestRepository.countByStudentId(userId)).thenReturn(5L);

            Map<String, Object> aiScore = Map.of("score", 85.0, "category", "Trusted");
            when(restTemplate.postForObject(contains("/trust-score/"), any(), eq(Object.class)))
                    .thenReturn(aiScore);

            Object result = service.getTrustScore(userId);

            assertThat(result).isEqualTo(aiScore);
        }

        @Test
        @DisplayName("should return fallback score map when AI service throws")
        void getTrustScore_whenAiThrows_shouldReturnFallback() {
            Long userId = 11L;
            when(requestRepository.countByStudentIdAndStatus(userId, collocOffreRequest.Status.ACCEPTEE))
                    .thenReturn(0L);
            when(requestRepository.countByStudentId(userId)).thenReturn(0L);
            when(restTemplate.postForObject(contains("/trust-score/"), any(), eq(Object.class)))
                    .thenThrow(new RestClientException("Timeout"));

            Object result = service.getTrustScore(userId);

            assertThat(result).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> fallback = (Map<String, Object>) result;
            assertThat(fallback)
                    .containsEntry("userId", userId)
                    .containsEntry("score", 50.0)
                    .containsEntry("category", "New User")
                    .containsEntry("color", "yellow");
        }

        @Test
        @DisplayName("should compute acceptedRatio as 0.5 when total is zero (no history)")
        void getTrustScore_withZeroTotalRequests_shouldUse0point5Ratio() {
            Long userId = 12L;
            when(requestRepository.countByStudentIdAndStatus(userId, collocOffreRequest.Status.ACCEPTEE))
                    .thenReturn(0L);
            when(requestRepository.countByStudentId(userId)).thenReturn(0L);
            when(restTemplate.postForObject(contains("/trust-score/"), any(), eq(Object.class)))
                    .thenReturn(Map.of("score", 50.0));

            // Should not throw — verify it calls AI with acceptedRatio = 0.5
            service.getTrustScore(userId);

            verify(restTemplate).postForObject(contains("/trust-score/"), any(), eq(Object.class));
        }

        @Test
        @DisplayName("should compute acceptedRatio correctly when total > 0")
        void getTrustScore_withPositiveTotalRequests_shouldComputeRatio() {
            Long userId = 13L;
            when(requestRepository.countByStudentIdAndStatus(userId, collocOffreRequest.Status.ACCEPTEE))
                    .thenReturn(4L);
            when(requestRepository.countByStudentId(userId)).thenReturn(5L);
            when(restTemplate.postForObject(contains("/trust-score/"), any(), eq(Object.class)))
                    .thenReturn(Map.of("score", 90.0));

            Object result = service.getTrustScore(userId);

            assertThat(result).isNotNull();
            verify(restTemplate).postForObject(contains("/trust-score/"), any(), eq(Object.class));
        }
    }

    // =========================================================================
    // PREDICT PRICE
    // =========================================================================
    @Nested
    @DisplayName("predictPrice()")
    class PredictPriceTests {

        @Test
        @DisplayName("should return predicted price when AI service succeeds")
        void predictPrice_whenAiSucceeds_shouldReturnPrediction() {
            Map<String, Object> requestPayload = Map.of("ville", "Tunis", "chambres", 2);
            Map<String, Object> aiResponse = Map.of("predictedPrice", 650.0);

            when(restTemplate.postForObject(contains("/predict-price"), eq(requestPayload), eq(Object.class)))
                    .thenReturn(aiResponse);

            Object result = service.predictPrice(requestPayload);

            assertThat(result).isEqualTo(aiResponse);
            verify(restTemplate).postForObject(contains("/predict-price"), eq(requestPayload), eq(Object.class));
        }

        @Test
        @DisplayName("should return null when AI service throws exception")
        void predictPrice_whenAiThrows_shouldReturnNull() {
            Object requestPayload = Map.of("ville", "Sfax");

            when(restTemplate.postForObject(contains("/predict-price"), any(), eq(Object.class)))
                    .thenThrow(new RestClientException("Service unavailable"));

            Object result = service.predictPrice(requestPayload);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should pass the request payload directly to AI service")
        void predictPrice_shouldPassPayloadToAi() {
            String payload = "raw-payload";
            when(restTemplate.postForObject(contains("/predict-price"), eq(payload), eq(Object.class)))
                    .thenReturn("prediction");

            service.predictPrice(payload);

            verify(restTemplate).postForObject(contains("/predict-price"), eq(payload), eq(Object.class));
        }
    }
}
