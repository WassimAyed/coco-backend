package tn.esprit.subspaymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.subspaymentservice.entity.SubscriptionStatus;
import tn.esprit.subspaymentservice.entity.UserSubscription;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
    Optional<UserSubscription> findFirstByUserIdOrderByStartDateDesc(Long userId);
    List<UserSubscription> findByUserId(Long userId);
}
