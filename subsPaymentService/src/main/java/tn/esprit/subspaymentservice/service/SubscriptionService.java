package tn.esprit.subspaymentservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.subspaymentservice.entity.SubscriptionPlan;
import tn.esprit.subspaymentservice.entity.SubscriptionStatus;
import tn.esprit.subspaymentservice.entity.UserSubscription;
import tn.esprit.subspaymentservice.repository.SubscriptionPlanRepository;
import tn.esprit.subspaymentservice.repository.UserSubscriptionRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Transactional
    public UserSubscription createFreeSubscription(Long userId) {
        SubscriptionPlan freePlan = subscriptionPlanRepository.findByName("FREE")
                .orElseThrow(() -> new RuntimeException("Plan FREE introuvable"));

        UserSubscription sub = UserSubscription.builder()
                .userId(userId)
                .plan(freePlan)
                .startDate(new Date())
            .status(SubscriptionStatus.ACTIVE)
                .remainingPosts(freePlan.getPostLimit())
                .build();

        if (freePlan.getDurationDays() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(sub.getStartDate());
            cal.add(java.util.Calendar.DAY_OF_YEAR, freePlan.getDurationDays());
            sub.setEndDate(cal.getTime());
        }

        return userSubscriptionRepository.save(sub);
    }

    public List<SubscriptionPlan> getAllPlans() {
        return subscriptionPlanRepository.findAll();
    }

    public Optional<SubscriptionPlan> getPlanById(Long id) {
        return subscriptionPlanRepository.findById(id);
    }

    @Transactional
    public SubscriptionPlan savePlan(SubscriptionPlan plan) {
        return subscriptionPlanRepository.save(plan);
    }

    @Transactional
    public SubscriptionPlan updatePlan(Long id, SubscriptionPlan planDetails) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan non trouvé avec l'id : " + id));
        
        plan.setName(planDetails.getName());
        plan.setPrice(planDetails.getPrice());
        plan.setPostLimit(planDetails.getPostLimit());
        plan.setDurationDays(planDetails.getDurationDays());
        plan.setType(planDetails.getType());
        
        return subscriptionPlanRepository.save(plan);
    }

    @Transactional
    public void deletePlan(Long id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan non trouvé avec l'id : " + id));
        subscriptionPlanRepository.delete(plan);
    }

    public Map<String, Object> checkQuota(Long userId) {
        Optional<UserSubscription> activeSub = userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        Date now = new Date();
        
        Map<String, Object> result = new HashMap<>();
        if (activeSub.isPresent()) {
            UserSubscription sub = activeSub.get();
            
            // Vérification de l'expiration par date
            if (sub.getEndDate() != null && now.after(sub.getEndDate())) {
                sub.setStatus(SubscriptionStatus.EXPIRED);
                userSubscriptionRepository.save(sub);
                result.put("remaining_posts", 0);
                result.put("canPost", false);
                result.put("reason", "Abonnement expiré");
                return result;
            }

            result.put("remaining_posts", sub.getRemainingPosts());
            // canPost est vrai si remainingPosts est null (illimité) ou > 0
            result.put("canPost", sub.getRemainingPosts() == null || sub.getRemainingPosts() > 0);
        } else {
            result.put("remaining_posts", 0);
            result.put("canPost", false);
        }
        return result;
    }

    @Transactional
    public void consumePost(Long userId) {
        UserSubscription sub = userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Aucun abonnement actif trouvé"));

        Date now = new Date();
        if (sub.getEndDate() != null && now.after(sub.getEndDate())) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            userSubscriptionRepository.save(sub);
            throw new RuntimeException("Abonnement expiré");
        }

        if (sub.getRemainingPosts() != null) {
            if (sub.getRemainingPosts() <= 0) {
                throw new RuntimeException("Quota dépassé");
            }
            sub.setRemainingPosts(sub.getRemainingPosts() - 1);
            userSubscriptionRepository.save(sub);
        }
    }

    @Transactional
    public void addBonusPosts(Long userId, int bonus) {
        UserSubscription sub = userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Aucun abonnement actif trouvé"));
        
        if (sub.getRemainingPosts() != null) {
            sub.setRemainingPosts(sub.getRemainingPosts() + bonus);
        } else {
            // Si c'était illimité, on ne fait rien ou on définit un quota (mais illimité est mieux)
            // Pour l'instant on garde illimité si c'est null
        }
        userSubscriptionRepository.save(sub);
    }
}
