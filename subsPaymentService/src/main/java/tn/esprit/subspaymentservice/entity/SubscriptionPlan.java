package tn.esprit.subspaymentservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // FREE, MONTHLY, YEARLY, PAY_PER_POST

    @Column(nullable = false)
    private Double price;

    private Integer postLimit; // Nullable, only for FREE or PAY_PER_POST

    private Integer durationDays; // Nullable, only for MONTHLY/YEARLY

    @Column(nullable = false)
    private String type; // FREE, SUBSCRIPTION, PAY_PER_POST
}
