package tn.esprit.eventservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.eventservice.entity.EventStatus;
import tn.esprit.eventservice.entity.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {

    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    private String name;

    @NotBlank(message = "La localisation est obligatoire")
    private String location;

    @Schema(description = "Latitude WGS-84 (optionnelle, fournie par la carte OSM)", example = "36.8665")
    private Double latitude;

    @Schema(description = "Longitude WGS-84 (optionnelle, fournie par la carte OSM)", example = "10.1647")
    private Double longitude;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    @Schema(description = "URL de l'image principale de l'événement")
    private String imageUrl;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;

    @NotNull(message = "La capacité maximale est obligatoire")
    @Min(value = 1, message = "La capacité doit être au moins 1")
    private Integer maxCapacity;

    private Integer currentParticipants;

    private Long userId;

    @NotNull(message = "Le statut est obligatoire")
    private EventStatus status;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categoryId;

    @NotNull(message = "Le type d'événement est obligatoire")
    private EventType eventType;

    @DecimalMin(value = "0.0", message = "Le prix doit être supérieur ou égal à 0")
    @Schema(description = "Prix de l'événement en TND", example = "25.50")
    private BigDecimal price;

    @Schema(description = "Température moyenne prévue pour la date de l'événement (°C)", example = "23.4")
    private Double temperature;

    @Schema(description = "Précipitations prévues pour la date de l'événement (mm)", example = "1.6")
    private Double precipitationMm;

    @Schema(description = "Vitesse du vent prévue pour la date de l'événement (km/h)", example = "18.2")
    private Double windSpeedKmh;

    @Schema(description = "Nombre de participants prédit par le modèle ML", example = "120")
    private Integer predictedParticipants;

    @Schema(description = "Code météo Open-Meteo (WMO)", example = "3")
    private Integer weatherCode;

    @Schema(description = "Libellé météo lisible (Clear, Rain, Clouds...)", example = "Clouds")
    private String weatherLabel;

    // Lombok handles boilerplate accessors/builders for this DTO.
}