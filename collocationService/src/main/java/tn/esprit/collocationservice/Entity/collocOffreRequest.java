package tn.esprit.collocationservice.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class collocOffreRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many requests can belong to one offer
    @ManyToOne
    @JoinColumn(name = "offer_id", nullable = false)
    private collocOffre offer;

    private Long studentId;

    private String message;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;

    public enum Status {
        ENCOURS, ACCEPTEE, REJETEE
    }
}