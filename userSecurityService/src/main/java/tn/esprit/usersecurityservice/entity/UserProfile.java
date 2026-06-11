package tn.esprit.usersecurityservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 relation
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"roommateProfile", "password", "authorities"}) // Add this!
    private User user;

    @Column(nullable = false)
    private Integer age;

    private String gender;
    private Double budget;
    private String city;
    private Boolean smoker;
    private Boolean pets;
    private Integer cleanliness;
    private String sleepSchedule;
    private String studyLevel;

    private Integer socialLevel;
    private Boolean acceptsGuests;
    private Integer noiseTolerance;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_profile_interests",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "interest")
    private List<String> interests;

    private Double latitude;
    private Double longitude;
}