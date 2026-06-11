package tn.esprit.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorDTO {

    private Long userId;
    private Long eventId;
    private Long categoryId;
    private String actionType;
    private Double lat;
    private Double lng;
}
