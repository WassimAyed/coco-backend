package tn.esprit.eventservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "event_gallery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventGallery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "public_id")
    private String publicId;  // pour suppression Cloudinary

    private String caption;   // légende optionnelle

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}