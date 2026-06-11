package tn.esprit.covoiturageservice.Service;

import tn.esprit.covoiturageservice.Entity.Covoiturage;
import java.util.List;

public interface ICovoiturageService {

    Covoiturage getCovoiturageById(Long id);
    List<Covoiturage> getCovoiturageByIdDriver(Long idDriver);

    List<Covoiturage> getAllCovoiturage();
    void deleteCovoiturage(Long id);
    Covoiturage addCovoiturage(Covoiturage covoiturage);
    Covoiturage updateCovoiturage(Covoiturage covoiturage);

    List<Covoiturage> getSimilarCovoiturages(Long id, int limit);

    tn.esprit.covoiturageservice.Dto.AdminStatsDTO getAdminStats();
}
