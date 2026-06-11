package tn.esprit.realestateservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "furniture_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FurnitureImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "furniture_id")
    private Furniture furniture;
}
