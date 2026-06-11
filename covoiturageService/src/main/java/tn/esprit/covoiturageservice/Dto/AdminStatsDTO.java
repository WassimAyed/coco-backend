package tn.esprit.covoiturageservice.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private long totalTrajets;
    private long conducteursActifs;
    private long placesDisponibles;
}
