package tn.esprit.eventservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_behavior", indexes = {
        @Index(name = "idx_user_behavior_user_id", columnList = "user_id"),
        @Index(name = "idx_user_behavior_event_id", columnList = "event_id"),
        @Index(name = "idx_user_behavior_action_type", columnList = "action_type"),
        @Index(name = "idx_user_behavior_user_action", columnList = "user_id,action_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Column(name = "last_lat")
    private Double lastLat;

    @Column(name = "last_lng")
    private Double lastLng;

    @Column(name = "action_date", nullable = false)
    private LocalDateTime actionDate;

    @PrePersist
    void prePersist() {
        if (actionDate == null) {
            actionDate = LocalDateTime.now();
        }
    }
}
