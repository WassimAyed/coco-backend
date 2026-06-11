package tn.esprit.eventservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventGallery> gallery = new ArrayList<>();

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "current_participants")
    @Builder.Default
    private Integer currentParticipants = 0;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "precipitation_mm")
    private Double precipitationMm;

    @Column(name = "wind_speed_kmh")
    private Double windSpeedKmh;

    @Column(name = "predicted_participants")
    private Integer predictedParticipants;

    @Column(name = "weather_code")
    private Integer weatherCode;

    @Column(name = "weather_label", length = 50)
    private String weatherLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 10)
    private EventType eventType;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @JsonIgnore
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Reaction> reactions = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Participant> participants = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventRating> ratings = new ArrayList<>();
}
