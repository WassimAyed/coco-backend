package tn.esprit.collocationservice.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Repository.collocOffreRepo;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CollocOffreExpirationScheduler {

    private final collocOffreRepo repo;
    private final EmailService emailService;
    private final UserSecurityClient userSecurityClient;

    /**
     * Runs immediately when the server starts and then every hour while the server is running.
     * Processes expired offers (date <= today and notified = false) and sends email notifications.
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 0)   // every hour, start at once
    @Transactional
    public void processExpiredOffers() {
        log.info("Starting scheduled task to detect and notify expired offers...");

        List<collocOffre> expiredOffers = repo.findExpiredAndUnnotifiedOffers(LocalDate.now());

        if (expiredOffers.isEmpty()) {
            log.info("No expired offers found today.");
            return;
        }

        for (collocOffre offer : expiredOffers) {
            try {
                if (offer.getOwnerId() != null) {
                    UserDTO user = userSecurityClient.findUserById(offer.getOwnerId());

                    if (user != null && user.getEmail() != null) {
                        emailService.sendOfferExpiredEmail(user.getEmail(), offer.getTitre());
                        offer.setNotified(true);
                        repo.save(offer);
                        log.info("Notification sent for offer ID {} to user ID {}", offer.getId(), offer.getOwnerId());
                    } else {
                        log.warn("User or email not found for owner ID {} on offer ID {}", offer.getOwnerId(), offer.getId());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process expiration for offer ID {}", offer.getId(), e);
            }
        }

        log.info("Finished processing {} expired offers.", expiredOffers.size());
    }
}