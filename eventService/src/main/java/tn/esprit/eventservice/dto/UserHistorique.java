package tn.esprit.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserHistorique {

    private Long userId;
    private Map<Long, Long> categoryFrequency;
    private Double preferredLat;
    private Double preferredLng;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String preferredEventType;
}
