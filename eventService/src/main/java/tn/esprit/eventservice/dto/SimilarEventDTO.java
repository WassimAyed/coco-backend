package tn.esprit.eventservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SimilarEventDTO {
    private Long id;
    private String name;
    private String location;
    private String imageUrl;
    private LocalDateTime startDate;
    private Integer maxCapacity;
    private Integer currentParticipants;
    private String categoryName;
    private Double averageRating;
    private String similarityReason;
}