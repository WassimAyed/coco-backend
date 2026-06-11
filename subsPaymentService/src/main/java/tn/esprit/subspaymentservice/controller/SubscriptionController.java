package tn.esprit.subspaymentservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.subspaymentservice.entity.SubscriptionPlan;
import tn.esprit.subspaymentservice.entity.UserSubscription;
import tn.esprit.subspaymentservice.repository.UserSubscriptionRepository;
import tn.esprit.subspaymentservice.service.SubscriptionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserSubscriptionRepository userSubRepository;

    @GetMapping("/subscriptions")
    public List<SubscriptionPlan> getAllPlans() {
        return subscriptionService.getAllPlans();
    }

    @PostMapping("/subscriptions")
    public SubscriptionPlan createPlan(@RequestBody SubscriptionPlan plan) {
        return subscriptionService.savePlan(plan);
    }

    @GetMapping("/subscriptions/{id}")
    public ResponseEntity<SubscriptionPlan> getPlanById(@PathVariable Long id) {
        return subscriptionService.getPlanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/subscriptions/{id}")
    public ResponseEntity<SubscriptionPlan> updatePlan(@PathVariable Long id, @RequestBody SubscriptionPlan planDetails) {
        try {
            return ResponseEntity.ok(subscriptionService.updatePlan(id, planDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/subscriptions/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        try {
            subscriptionService.deletePlan(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user-subscriptions")
    public List<UserSubscription> getAllUserSubscriptions() {
        return userSubRepository.findAll();
    }

    @GetMapping("/user-subscriptions/user/{userId}")
    public ResponseEntity<List<UserSubscription>> getSubscriptionsByUserId(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long requesterId) {
        if (requesterId == null || !requesterId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<UserSubscription> subs = userSubRepository.findByUserId(userId);
        if (subs.isEmpty()) {
            UserSubscription freeSub = subscriptionService.createFreeSubscription(userId);
            return ResponseEntity.ok(List.of(freeSub));
        }
        return ResponseEntity.ok(subs);
    }

    @PostMapping("/user-subscriptions")
    public ResponseEntity<UserSubscription> createUserSubscription(
            @RequestBody Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long requesterId) {
        if (requesterId == null || !requesterId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(subscriptionService.createFreeSubscription(userId));
    }

    @GetMapping("/quota/{userId}")
    public ResponseEntity<Map<String, Object>> getQuota(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long requesterId) {
        if (requesterId == null || !requesterId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(subscriptionService.checkQuota(userId));
    }

    @PostMapping("/consume/{userId}")
    public ResponseEntity<Void> consume(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long requesterId) {
        if (requesterId == null || !requesterId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            subscriptionService.consumePost(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/admin/bonus/{userId}")
    public ResponseEntity<Void> addBonus(@PathVariable Long userId, @RequestParam int amount) {
        try {
            subscriptionService.addBonusPosts(userId, amount);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
