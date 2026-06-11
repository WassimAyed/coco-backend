package tn.esprit.collocationservice.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class UserActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // the user who performed the action
    private Long offerId; // the collocation offer interacted with
    
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    private LocalDateTime timestamp;

    public enum ActivityType {
        VIEW, FAVORITE, APPLY, CLICK
    }
}
