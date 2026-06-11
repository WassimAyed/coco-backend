package tn.esprit.subspaymentservice.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.subspaymentservice.entity.Payment;
import tn.esprit.subspaymentservice.entity.PaymentStatus;
import tn.esprit.subspaymentservice.entity.SubscriptionPlan;
import tn.esprit.subspaymentservice.entity.SubscriptionStatus;
import tn.esprit.subspaymentservice.entity.UserSubscription;
import tn.esprit.subspaymentservice.repository.PaymentRepository;
import tn.esprit.subspaymentservice.repository.SubscriptionPlanRepository;
import tn.esprit.subspaymentservice.repository.UserSubscriptionRepository;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final DiscordService discordService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${frontend.url}")
    private String frontendUrl;

    public String createCheckoutSession(Long userId, Long planId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan non trouvé"));

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/subs-payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/subs-payment/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount((long) (plan.getPrice() * 100))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Abonnement " + plan.getName())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("userId", userId.toString())
                .putMetadata("planId", planId.toString())
                .build();

        try {
            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            System.err.println("STRIPE ERROR: " + e.getMessage());
            log.error("Erreur lors de la création de la session Stripe", e);
            throw new RuntimeException("Erreur Stripe: " + e.getMessage());
        }
    }

    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        try {
            if (sigHeader == null || webhookSecret == null || webhookSecret.isBlank()) {
                throw new RuntimeException("Missing Stripe webhook signature");
            }

            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if (event != null && "checkout.session.completed".equals(event.getType())) {
                log.info("Traitement d une session Stripe complétée");
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null) {
                    processSuccessfulPayment(session);
                } else {
                    log.error("Impossible de récupérer la session dans l événement");
                }
            }
        } catch (Exception e) {
            log.error("Erreur critique Webhook Stripe: {}", e.getMessage());
            throw new RuntimeException("Webhook error: " + e.getMessage());
        }
    }

    private void processSuccessfulPayment(Session session) {
        if (paymentRepository.findByStripePaymentId(session.getId()).isPresent()) {
            log.info("Stripe session {} already processed, skipping duplicate processing", session.getId());
            return;
        }

        Long userId = Long.valueOf(session.getMetadata().get("userId"));
        Long planId = Long.valueOf(session.getMetadata().get("planId"));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan non trouvé"));

        UserSubscription sub = userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
            .orElse(UserSubscription.builder().userId(userId).status(SubscriptionStatus.ACTIVE).build());

        sub.setPlan(plan);
        sub.setStartDate(new Date());

        if (plan.getDurationDays() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(sub.getStartDate());
            cal.add(java.util.Calendar.DAY_OF_YEAR, plan.getDurationDays());
            sub.setEndDate(cal.getTime());
        } else {
            sub.setEndDate(null);
        }

        if (plan.getPostLimit() != null) {
            sub.setRemainingPosts(plan.getPostLimit());
        } else {
            sub.setRemainingPosts(null);
        }

        userSubscriptionRepository.save(sub);

        Payment payment = Payment.builder()
                .userId(userId)
                .subscription(sub)
                .amount(plan.getPrice())
                .currency("TND")
                .status(PaymentStatus.SUCCESS)
                .stripePaymentId(session.getId())
                .build();
        paymentRepository.save(payment);

        log.info("Paiement réussi et abonnement activé pour l'utilisateur {}", userId);
        discordService.sendSuccessPayment(plan.getName(), plan.getPrice(), userId);
    }

    @Transactional
    public void confirmPaymentSession(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            if ("paid".equals(session.getPaymentStatus())) {
                processSuccessfulPayment(session);
            } else {
                throw new RuntimeException("Paiement non confirmé par Stripe");
            }
        } catch (StripeException e) {
            log.error("Erreur récupération session Stripe", e);
            throw new RuntimeException("Erreur Stripe: " + e.getMessage());
        }
    }
}
