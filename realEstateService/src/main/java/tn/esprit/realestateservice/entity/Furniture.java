package tn.esprit.realestateservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.realestateservice.entity.enums.FurnitureCondition;
import tn.esprit.realestateservice.entity.enums.FurnitureStatus;

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
    @Column(name = "`condition`")
    private FurnitureCondition condition;

    private Double price;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private FurnitureStatus status;

    private Long sellerId;
}
