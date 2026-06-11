package tn.esprit.eventservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.Data;


import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class EventRatingDTO {

    private Long id;

    @NotNull
    private Long eventId;

    @NotNull
    private Long userId;

    @NotNull
    @Min(1) @Max(5)
    private Integer rating;

    private LocalDateTime createdAt;
    private Double averageRating; // retourné en réponse
    private Long totalRatings;    // retourné en réponse

}
