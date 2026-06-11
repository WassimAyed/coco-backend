package tn.esprit.eventservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("price")
    private Double price;


    @JsonProperty("is_free")
    private Integer isFree;

    @JsonProperty("max_capacity")
    private Integer maxCapacity;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("is_weekend")
    private Integer isWeekend;

    @JsonProperty("is_holiday")
    private Integer isHoliday;

    @JsonProperty("duration_days")
    private Integer durationDays;

    @JsonProperty("days_until_event")
    private Integer daysUntilEvent;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("precipitation_mm")
    private Double precipitationMm;

    @JsonProperty("wind_speed_kmh")
    private Double windSpeedKmh;

}