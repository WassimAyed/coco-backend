package tn.esprit.covoiturageservice.Service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.esprit.covoiturageservice.Dto.CO2ImpactDTO;
import tn.esprit.covoiturageservice.Entity.Covoiturage;

import static org.assertj.core.api.Assertions.assertThat;

class CO2CalculatorServiceTest {

    private final CO2CalculatorService service = new CO2CalculatorService();

    @Test
    @DisplayName("compute calcule un trajet 100 km solo (zero passager)")
    void compute_solo() {
        Covoiturage trip = new Covoiturage();
        trip.setId(1L);
        trip.setDistance(100);

        CO2ImpactDTO impact = service.compute(trip, 0);

        // 100 km * 7L/100 * 2.31 = 16.17 kg
        assertThat(impact.getCo2SoloKg()).isEqualTo(16.17);
        assertThat(impact.getNombreOccupants()).isEqualTo(1);
        assertThat(impact.getCo2EconomiseTotalKg()).isZero();
    }

    @Test
    @DisplayName("compute renvoie une economie quand des passagers sont presents")
    void compute_withPassengers() {
        Covoiturage trip = new Covoiturage();
        trip.setId(1L);
        trip.setDistance(100);

        CO2ImpactDTO impact = service.compute(trip, 3);

        assertThat(impact.getNombreOccupants()).isEqualTo(4);
        assertThat(impact.getCo2EconomiseTotalKg()).isGreaterThan(0);
        assertThat(impact.getCo2ParPassagerKg())
                .isLessThan(impact.getCo2SoloKg());
    }

    @Test
    @DisplayName("compute renvoie equivalent arbres et km")
    void compute_returnsEquivalents() {
        Covoiturage trip = new Covoiturage();
        trip.setId(1L);
        trip.setDistance(200);

        CO2ImpactDTO impact = service.compute(trip, 2);

        assertThat(impact.getEquivalentArbresAn()).isGreaterThan(0);
        assertThat(impact.getEquivalentKmVoitureSolo()).isGreaterThan(0);
    }

    @Test
    @DisplayName("compute gere un trajet sans distance")
    void compute_zeroDistance() {
        Covoiturage trip = new Covoiturage();
        trip.setId(1L);
        trip.setDistance(0);

        CO2ImpactDTO impact = service.compute(trip, 2);

        assertThat(impact.getCo2SoloKg()).isZero();
        assertThat(impact.getCo2EconomiseTotalKg()).isZero();
    }
}
