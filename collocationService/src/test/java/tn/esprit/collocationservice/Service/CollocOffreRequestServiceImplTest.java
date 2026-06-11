package tn.esprit.collocationservice.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreRequest;
import tn.esprit.collocationservice.Repository.collocOffreRepo;
import tn.esprit.collocationservice.Repository.collocOffreRequestRepo;
import tn.esprit.collocationservice.Exception.OfferNotFoundException;
import tn.esprit.collocationservice.Exception.RequestNotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("collocOffreRequestServiceImpl Unit Tests")
class CollocOffreRequestServiceImplTest {

    @Mock
    private collocOffreRequestRepo repo;

    @Mock
    private collocOffreRepo offreRepo;

    @InjectMocks
    private collocOffreRequestServiceImpl service;

    private collocOffre sampleOffer;
    private collocOffreRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleOffer = new collocOffre();
        sampleOffer.setId(1L);
        sampleOffer.setTitre("Studio Tunis");
        sampleOffer.setOwnerId(100L);

        sampleRequest = new collocOffreRequest();
        sampleRequest.setId(10L);
        sampleRequest.setOffer(sampleOffer);
        sampleRequest.setStudentId(200L);
        sampleRequest.setMessage("Je suis intéressé par cette collocation");
        sampleRequest.setStatus(collocOffreRequest.Status.ENCOURS);
        sampleRequest.setCreatedAt(LocalDateTime.of(2026, 4, 10, 14, 30));
    }

    // =========================================================================
    // CREATE
    // =========================================================================
    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create request with status ENCOURS and correct associations")
        void create_shouldSetStatusAndStudentIdAndSave() {
            // Arrange
            collocOffreRequest input = new collocOffreRequest();
            collocOffre offerRef = new collocOffre();
            offerRef.setId(1L);
            input.setOffer(offerRef);
            input.setMessage("Bonjour, je cherche une collocation!");

            Long userId = 200L;

            when(offreRepo.findById(1L)).thenReturn(Optional.of(sampleOffer));
            when(repo.save(any(collocOffreRequest.class))).thenAnswer(invocation -> {
                collocOffreRequest saved = invocation.getArgument(0);
                saved.setId(50L);
                return saved;
            });

            // Act
            collocOffreRequest result = service.create(input, userId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(50L);
            assertThat(result.getStudentId()).isEqualTo(userId);
            assertThat(result.getStatus()).isEqualTo(collocOffreRequest.Status.ENCOURS);
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getOffer()).isEqualTo(sampleOffer);
            assertThat(result.getMessage()).isEqualTo("Bonjour, je cherche une collocation!");

            // Verify the offer was fetched from DB (not the shallow ref)
            verify(offreRepo).findById(1L);
            verify(repo).save(any(collocOffreRequest.class));
        }

        @Test
        @DisplayName("should throw OfferNotFoundException when offer does not exist")
        void create_whenOfferNotFound_shouldThrowException() {
            collocOffreRequest input = new collocOffreRequest();
            collocOffre offerRef = new collocOffre();
            offerRef.setId(999L);
            input.setOffer(offerRef);

            when(offreRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(input, 200L))
                    .isInstanceOf(OfferNotFoundException.class)
                    .hasMessageContaining("Offer not found");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when offer is missing")
        void create_whenOfferIsMissing_shouldThrowException() {
            collocOffreRequest input = new collocOffreRequest();

            assertThatThrownBy(() -> service.create(input, 200L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Offer information is missing");

            verifyNoInteractions(offreRepo);
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when offer id is missing")
        void create_whenOfferIdIsMissing_shouldThrowException() {
            collocOffreRequest input = new collocOffreRequest();
            input.setOffer(new collocOffre());

            assertThatThrownBy(() -> service.create(input, 200L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Offer information is missing");

            verifyNoInteractions(offreRepo);
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("should set createdAt to approximately now")
        void create_shouldSetCreatedAtToNow() {
            collocOffreRequest input = new collocOffreRequest();
            collocOffre offerRef = new collocOffre();
            offerRef.setId(1L);
            input.setOffer(offerRef);

            when(offreRepo.findById(1L)).thenReturn(Optional.of(sampleOffer));
            when(repo.save(any(collocOffreRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            collocOffreRequest result = service.create(input, 200L);
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertThat(result.getCreatedAt()).isBetween(before, after);
        }
    }

    // =========================================================================
    // GET REQUESTS BY STUDENT
    // =========================================================================
    @Nested
    @DisplayName("getRequestsByStudent()")
    class GetRequestsByStudentTests {

        @Test
        @DisplayName("should return all requests for a given student")
        void getRequestsByStudent_shouldReturnStudentRequests() {
            when(repo.findByStudentId(200L)).thenReturn(List.of(sampleRequest));

            List<collocOffreRequest> result = service.getRequestsByStudent(200L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudentId()).isEqualTo(200L);
            verify(repo).findByStudentId(200L);
        }

        @Test
        @DisplayName("should return empty list when student has no requests")
        void getRequestsByStudent_whenNone_shouldReturnEmpty() {
            when(repo.findByStudentId(999L)).thenReturn(Collections.emptyList());

            List<collocOffreRequest> result = service.getRequestsByStudent(999L);

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // UPDATE STATUS
    // =========================================================================
    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatusTests {

        @Test
        @DisplayName("should update status to ACCEPTEE")
        void updateStatus_toAccepted_shouldUpdateAndSave() {
            when(repo.findById(10L)).thenReturn(Optional.of(sampleRequest));
            when(repo.save(any(collocOffreRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

            collocOffreRequest result = service.updateStatus(10L, collocOffreRequest.Status.ACCEPTEE);

            assertThat(result.getStatus()).isEqualTo(collocOffreRequest.Status.ACCEPTEE);
            verify(repo).findById(10L);
            verify(repo).save(sampleRequest);
        }

        @Test
        @DisplayName("should update status to REJETEE")
        void updateStatus_toRejected_shouldUpdateAndSave() {
            when(repo.findById(10L)).thenReturn(Optional.of(sampleRequest));
            when(repo.save(any(collocOffreRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

            collocOffreRequest result = service.updateStatus(10L, collocOffreRequest.Status.REJETEE);

            assertThat(result.getStatus()).isEqualTo(collocOffreRequest.Status.REJETEE);
        }

        @Test
        @DisplayName("should throw RequestNotFoundException when request not found")
        void updateStatus_whenNotFound_shouldThrow() {
            when(repo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateStatus(999L, collocOffreRequest.Status.ACCEPTEE))
                    .isInstanceOf(RequestNotFoundException.class)
                    .hasMessageContaining("Request not found");

            verify(repo, never()).save(any());
        }
    }

    // =========================================================================
    // GET REQUESTS BY OFFER OWNER
    // =========================================================================
    @Nested
    @DisplayName("getRequestsByOfferOwner()")
    class GetRequestsByOfferOwnerTests {

        @Test
        @DisplayName("should return all requests for offers owned by given user")
        void getRequestsByOfferOwner_shouldReturnRequests() {
            when(repo.findByOfferOwnerId(100L)).thenReturn(List.of(sampleRequest));

            List<collocOffreRequest> result = service.getRequestsByOfferOwner(100L);

            assertThat(result).hasSize(1);
            verify(repo).findByOfferOwnerId(100L);
        }

        @Test
        @DisplayName("should return empty list when owner has no incoming requests")
        void getRequestsByOfferOwner_whenNone_shouldReturnEmpty() {
            when(repo.findByOfferOwnerId(999L)).thenReturn(Collections.emptyList());

            List<collocOffreRequest> result = service.getRequestsByOfferOwner(999L);

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // DELETE REQUEST
    // =========================================================================
    @Nested
    @DisplayName("deleteRequest()")
    class DeleteRequestTests {

        @Test
        @DisplayName("should call deleteById on repository when request exists")
        void deleteRequest_shouldCallRepoDeleteById() {
            when(repo.existsById(10L)).thenReturn(true);
            doNothing().when(repo).deleteById(10L);

            service.deleteRequest(10L);

            verify(repo).deleteById(10L);
        }

        @Test
        @DisplayName("should throw RequestNotFoundException when deleting non-existent request")
        void deleteRequest_whenNotFound_shouldThrow() {
            when(repo.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteRequest(999L))
                    .isInstanceOf(RequestNotFoundException.class)
                    .hasMessageContaining("Request not found");

            verify(repo, never()).deleteById(anyLong());
        }
    }

    // =========================================================================
    // GET REQUESTS BY OFFER IDS
    // =========================================================================
    @Nested
    @DisplayName("getRequestsByOfferIds()")
    class GetRequestsByOfferIdsTests {

        @Test
        @DisplayName("should filter requests by offer IDs and owner")
        void getRequestsByOfferIds_shouldFilterCorrectly() {
            // Arrange — offer owned by user 100
            collocOffreRequest req1 = new collocOffreRequest();
            req1.setId(1L);
            collocOffre offer1 = new collocOffre();
            offer1.setId(1L);
            offer1.setOwnerId(100L);
            req1.setOffer(offer1);

            // Arrange — offer owned by a different user
            collocOffreRequest req2 = new collocOffreRequest();
            req2.setId(2L);
            collocOffre offer2 = new collocOffre();
            offer2.setId(2L);
            offer2.setOwnerId(999L); // different owner
            req2.setOffer(offer2);

            // Arrange — offer owned by user 100 but NOT in the requested IDs
            collocOffreRequest req3 = new collocOffreRequest();
            req3.setId(3L);
            collocOffre offer3 = new collocOffre();
            offer3.setId(3L);
            offer3.setOwnerId(100L);
            req3.setOffer(offer3);

            when(repo.findAll()).thenReturn(List.of(req1, req2, req3));

            // Act — only interested in offer IDs [1, 2], owned by 100
            List<collocOffreRequest> result = service.getRequestsByOfferIds(List.of(1L, 2L), 100L);

            // Assert — only req1 matches (offer ID in list AND ownerId == 100)
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should return empty when no offers match")
        void getRequestsByOfferIds_whenNoMatch_shouldReturnEmpty() {
            when(repo.findAll()).thenReturn(Collections.emptyList());

            List<collocOffreRequest> result = service.getRequestsByOfferIds(List.of(1L), 100L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should exclude requests where ownerId does not match")
        void getRequestsByOfferIds_wrongOwner_shouldExclude() {
            collocOffreRequest req = new collocOffreRequest();
            req.setId(1L);
            collocOffre offer = new collocOffre();
            offer.setId(5L);
            offer.setOwnerId(200L); // different from requested owner
            req.setOffer(offer);

            when(repo.findAll()).thenReturn(List.of(req));

            List<collocOffreRequest> result = service.getRequestsByOfferIds(List.of(5L), 100L);

            assertThat(result).isEmpty();
        }
    }
}
