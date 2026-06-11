package tn.esprit.covoiturageservice.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CO2ImpactDTO {

    private Long covoiturageId;
    private double distanceKm;
    private int nombreOccupants;
    private double consommationLitres100km;
    private double facteurCo2KgParLitre;

    private double co2SoloKg;
    private double co2ParPassagerKg;
    private double co2EconomiseParPassagerKg;
    private double co2EconomiseTotalKg;

    private double equivalentArbresAn;
    private double equivalentKmVoitureSolo;
}
