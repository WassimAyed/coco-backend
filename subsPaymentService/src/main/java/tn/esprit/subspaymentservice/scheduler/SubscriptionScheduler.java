package tn.esprit.subspaymentservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprit.subspaymentservice.entity.SubscriptionStatus;
import tn.esprit.subspaymentservice.entity.UserSubscription;
import tn.esprit.subspaymentservice.repository.UserSubscriptionRepository;
import tn.esprit.subspaymentservice.service.EmailService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final EmailService emailService;

    // Toutes les 24h à minuit (ou toutes les minutes pour la démo si besoin, ici on met une fois par jour)
    @Scheduled(cron = "0 0 0 * * *")
    public void checkExpirations() {
        log.info("Running expiration check scheduler...");
        
        LocalDate targetDate = LocalDate.now().plusDays(3);
        List<UserSubscription> activeSubs = userSubscriptionRepository.findAll()
                .stream()
            .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE && s.getEndDate() != null)
                .toList();

        for (UserSubscription sub : activeSubs) {
            LocalDate endDate = sub.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (endDate.equals(targetDate)) {
                log.info("Subscription for user {} expires in 3 days. Sending alert.", sub.getUserId());
                String dummyEmail = "user_" + sub.getUserId() + "@coco.tn";
                emailService.sendExpirationWarning(dummyEmail, sub.getPlan().getName(), 3);
            }
        }
    }
}
