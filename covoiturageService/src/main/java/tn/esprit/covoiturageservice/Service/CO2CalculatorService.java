package tn.esprit.covoiturageservice.Service;

import org.springframework.stereotype.Service;
import tn.esprit.covoiturageservice.Dto.CO2ImpactDTO;
import tn.esprit.covoiturageservice.Entity.Covoiturage;

@Service
public class CO2CalculatorService {

    private static final double DEFAULT_CONSOMMATION_L_PAR_100KM = 7.0;
    private static final double FACTEUR_CO2_ESSENCE_KG_PAR_LITRE = 2.31;
    private static final double CO2_ABSORBE_PAR_ARBRE_KG_AN = 25.0;
// Un arbre moyen (chêne, hêtre, en zone tempérée) absorbe environ 25 kg de CO2 par an via la photosynthèse. Cette valeur
//   vient d'études forestières (ADEME, IPCC).
    public CO2ImpactDTO compute(Covoiturage covoiturage, int placesReservees) {
        double distanceKm = covoiturage.getDistance();
        int occupants = Math.max(1, placesReservees + 1);

        double co2Solo = distanceKm * DEFAULT_CONSOMMATION_L_PAR_100KM / 100.0 * FACTEUR_CO2_ESSENCE_KG_PAR_LITRE;
        double co2ParPassager = co2Solo / occupants;
        double co2EconomiseParPassager = co2Solo - co2ParPassager;
        double co2EconomiseTotal = co2EconomiseParPassager * (occupants - 1);

        double equivalentArbres = co2EconomiseTotal / CO2_ABSORBE_PAR_ARBRE_KG_AN;
        // Il faut l'activité biologique equivalentArbres  pendant 365 jours (photosynthèse jour après jour) pour nettoyer de
        //  ▎ l'atmosphère les co2EconomiseTotal  kg de CO2 que tu as économisés avec ton covoiturage.

        double equivalentKm = co2EconomiseTotal / (DEFAULT_CONSOMMATION_L_PAR_100KM / 100.0 * FACTEUR_CO2_ESSENCE_KG_PAR_LITRE);

        return CO2ImpactDTO.builder()
                .covoiturageId(covoiturage.getId())
                .distanceKm(round(distanceKm))
                .nombreOccupants(occupants)
                .consommationLitres100km(DEFAULT_CONSOMMATION_L_PAR_100KM)
                .facteurCo2KgParLitre(FACTEUR_CO2_ESSENCE_KG_PAR_LITRE)
                .co2SoloKg(round(co2Solo))
                .co2ParPassagerKg(round(co2ParPassager))
                .co2EconomiseParPassagerKg(round(co2EconomiseParPassager))
                .co2EconomiseTotalKg(round(co2EconomiseTotal))
                .equivalentArbresAn(round(equivalentArbres))
                .equivalentKmVoitureSolo(round(equivalentKm))
                .build();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
