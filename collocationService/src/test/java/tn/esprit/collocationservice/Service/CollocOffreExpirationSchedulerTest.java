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
import tn.esprit.collocationservice.Repository.collocOffreRepo;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollocOffreExpirationScheduler Unit Tests")
class CollocOffreExpirationSchedulerTest {

    @Mock
    private collocOffreRepo repo;

    @Mock
    private EmailService emailService;

    @Mock
    private UserSecurityClient userSecurityClient;

    @InjectMocks
    private CollocOffreExpirationScheduler scheduler;

    private collocOffre expiredOffer;

    @BeforeEach
    void setUp() {
        expiredOffer = new collocOffre();
        expiredOffer.setId(1L);
        expiredOffer.setTitre("Studio Expiré");
        expiredOffer.setOwnerId(100L);
        expiredOffer.setExpiryDate(LocalDate.now().minusDays(1));
        expiredOffer.setNotified(false);
    }

    // =========================================================================
    // PROCESS EXPIRED OFFERS
    // =========================================================================
    @Nested
    @DisplayName("processExpiredOffers()")
    class ProcessExpiredOffersTests {

        @Test
        @DisplayName("should send email and mark as notified for expired offers")
        void processExpiredOffers_shouldSendEmailAndMarkNotified() {
            // Arrange
            UserDTO user = new UserDTO();
            user.setId(100L);
            user.setEmail("owner@example.com");

            when(repo.findExpiredAndUnnotifiedOffers(any(LocalDate.class)))
                    .thenReturn(List.of(expiredOffer));
            when(userSecurityClient.findUserById(100L)).thenReturn(user);
            when(repo.save(any(collocOffre.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            scheduler.processExpiredOffers();

            // Assert
            verify(emailService).sendOfferExpiredEmail("owner@example.com", "Studio Expiré");
            verify(repo).save(argThat(collocOffre::getNotified));
        }

        @Test
        @DisplayName("should do nothing when no expired offers exist")
        void processExpiredOffers_whenNoExpired_shouldDoNothing() {
            when(repo.findExpiredAndUnnotifiedOffers(any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            scheduler.processExpiredOffers();

            verifyNoInteractions(userSecurityClient);
            verifyNoInteractions(emailService);
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("should skip offer when ownerId is null")
        void processExpiredOffers_whenOwnerIdNull_shouldSkip() {
            expiredOffer.setOwnerId(null);

            when(repo.findExpiredAndUnnotifiedOffers(any(LocalDate.class)))
                    .thenReturn(List.of(expiredOffer));

            scheduler.processExpiredOffers();

            verifyNoInteractions(userSecurityClient);
            verifyNoInteractions(emailService);
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("should skip when user is not found (null from client)")
        void processExpiredOffers_whenUserNotFound_shouldSkipNotification() {
            when(repo.findExpiredAndUnnotifiedOffers(any(LocalDate.class)))
                    .thenReturn(List.of(expiredOffer));
            when(userSecurityClient.findUserById(100L)).thenReturn(null);

            scheduler.processExpiredOffers();

            verify(emailService, never()).sendOfferExpiredEmail(anyString(), anyString());
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("should skip when user email is null")
        void processExpiredOffers_whenEmailNull_shouldSkipNotification() {
            UserDTO user = new UserDTO();
            user.setId(100L);
            user.setEmail(null);

            when(repo.findExpiredAndUnnotifiedOffers(any(LocalDate.class)))
                    .thenReturn(List.of(expiredOffer));
            when(userSecurityClient.findUserById(100L)).thenReturn(user);

            scheduler.processExpiredOffers();

            verify(emailService, never()).sendOfferExpiredEmail(anyString(), anyString());
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("should continue processing other offers when one fails")
        void processExpiredOffers_whenOneFails_shouldContinueWithOthers() {
            // Arrange — two expired offers
            collocOffre expiredOffer2 = new collocOffre();
            expiredOffer2.setId(2L);
            expiredOffer2.setTitre("Villa Expirée");
            expiredOffer2.setOwnerId(200L);
            expiredOffer2.setNotified(false);

            UserDTO user1 = new UserDTO();
            user1.setId(100L);
            user1.setEmail("owner1@example.com");

            UserDTO user2 = new UserDTO();
            user2.setId(200L);
            user2.setEmail("owner2@example.com");

            when(repo.findExpiredAndUnnotifiedOffers(any(LocalDate.class)))
                    .thenReturn(List.of(expiredOffer, expiredOffer2));
            when(userSecurityClient.findUserById(100L)).thenReturn(user1);
            when(userSecurityClient.findUserById(200L)).thenReturn(user2);

            // First email throws, second should still succeed
            doThrow(new RuntimeException("SMTP error"))
                    .when(emailService).sendOfferExpiredEmail("owner1@example.com", "Studio Expiré");
            doNothing()
                    .when(emailService).sendOfferExpiredEmail("owner2@example.com", "Villa Expirée");
            when(repo.save(any(collocOffre.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            scheduler.processExpiredOffers();

            // Assert — second offer still processed
            verify(emailService).sendOfferExpiredEmail("owner2@example.com", "Villa Expirée");
            // Only the second offer should be saved as notified (first one threw)
            verify(repo, times(1)).save(expiredOffer2);
        }

        @Test
        @DisplayName("should process multiple expired offers successfully")
        void processExpiredOffers_multipleOffers_shouldProcessAll() {
            collocOffre expiredOffer2 = new collocOffre();
            expiredOffer2.setId(2L);
            expiredOffer2.setTitre("Appart Sfax");
            expiredOffer2.setOwnerId(200L);
            expiredOffer2.setNotified(false);

            UserDTO user1 = new UserDTO();
            user1.setId(100L);
            user1.setEmail("user1@test.com");

            UserDTO user2 = new UserDTO();
            user2.setId(200L);
            user2.setEmail("user2@test.com");

            when(repo.findExpiredAndUnnotifiedOffers(any(LocalDate.class)))
                    .thenReturn(List.of(expiredOffer, expiredOffer2));
            when(userSecurityClient.findUserById(100L)).thenReturn(user1);
            when(userSecurityClient.findUserById(200L)).thenReturn(user2);
            when(repo.save(any(collocOffre.class))).thenAnswer(invocation -> invocation.getArgument(0));

            scheduler.processExpiredOffers();

            verify(emailService, times(2)).sendOfferExpiredEmail(anyString(), anyString());
            verify(repo, times(2)).save(any(collocOffre.class));
        }
    }
}
