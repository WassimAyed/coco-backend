package tn.esprit.eventservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "author_email"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "author_email", nullable = false)
    private String authorEmail;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}