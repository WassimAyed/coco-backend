package com.example.realestateservice.entity;

import jakarta.persistence.*;
import lombok.*;

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
