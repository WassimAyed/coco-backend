package com.example.realestateservice.entity;

import com.example.realestateservice.entity.enums.Condition;
import com.example.realestateservice.entity.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "furnitures")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Furniture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private String category;

    @Enumerated(EnumType.STRING)
    private Condition condition;

    private Double price;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Long sellerId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "furniture", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FurnitureImage> images = new ArrayList<>();

    @OneToOne(mappedBy = "furniture", cascade = CascadeType.ALL, orphanRemoval = true)
    private Address address;

    @OneToMany(mappedBy = "furniture", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.AVAILABLE;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
