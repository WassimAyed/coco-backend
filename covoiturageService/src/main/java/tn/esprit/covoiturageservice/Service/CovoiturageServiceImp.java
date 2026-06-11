package tn.esprit.covoiturageservice.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.covoiturageservice.Dto.AdminStatsDTO;
import tn.esprit.covoiturageservice.Entity.Covoiturage;
import tn.esprit.covoiturageservice.Repository.ICovoiturageRepository;
import tn.esprit.covoiturageservice.Repository.IReservationRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CovoiturageServiceImp implements ICovoiturageService {

    private final ICovoiturageRepository covoiturageRepository;
    private final IReservationRepository reservationRepository;


    @Override
    public Covoiturage getCovoiturageById(Long id) {
        return covoiturageRepository.findById(id).orElse(null);
    }

    @Override
    public List getCovoiturageByIdDriver(Long idDriver) {
        return covoiturageRepository.findByIdDriver(idDriver);
    }

    @Override
    public List<Covoiturage> getAllCovoiturage() {
        return covoiturageRepository.findAll();
    }

    @Override
    public void deleteCovoiturage(Long id) {
        reservationRepository.deleteAll(reservationRepository.findByCovoiturageId(id));
        covoiturageRepository.deleteById(id);
    }

    @Override
    public Covoiturage addCovoiturage(Covoiturage covoiturage) {
        return covoiturageRepository.save(covoiturage);
    }

    @Override
    public Covoiturage updateCovoiturage(Covoiturage covoiturage) {
        return covoiturageRepository.save(covoiturage);
    }

    @Override
    public List<Covoiturage> getSimilarCovoiturages(Long id, int limit) {
        Covoiturage current = covoiturageRepository.findById(id).orElse(null);
        if (current == null) return List.of();

        String depart = normalize(current.getPointDepart());
        String arrivee = normalize(current.getPointArrivee());
        LocalDateTime now = LocalDateTime.now();
        int max = limit > 0 ? limit : 4;

        return covoiturageRepository.findAll().stream()
                .filter(c -> !c.getId().equals(current.getId()))
                .filter(c -> c.getPlacesDisponibles() > 0)
                .filter(c -> c.getDateDepart() != null && c.getDateDepart().isAfter(now))
                .map(c -> new Scored(c, score(normalize(c.getPointDepart()), normalize(c.getPointArrivee()), depart, arrivee)))
                .filter(s -> s.score > 0)
                .sorted(Comparator
                        .comparingInt((Scored s) -> s.score).reversed()
                        .thenComparing(s -> s.c.getDateDepart()))
                .limit(max)
                .map(s -> s.c)
                .collect(Collectors.toList());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static int score(String cDep, String cArr, String depart, String arrivee) {
        if (cDep.equals(depart) && cArr.equals(arrivee)) return 3;
        if (cDep.equals(depart) || cArr.equals(arrivee)) return 2;
        if (cDep.contains(depart) || cArr.contains(arrivee) || depart.contains(cDep) || arrivee.contains(cArr)) return 1;
        return 0;
    }

    private record Scored(Covoiturage c, int score) {}

    @Override
    public AdminStatsDTO getAdminStats() {
        long totalTrajets = covoiturageRepository.count();
        long conducteursActifs = covoiturageRepository.countDistinctDrivers();
        long placesDisponibles = covoiturageRepository.sumPlacesDisponibles();
        return new AdminStatsDTO(totalTrajets, conducteursActifs, placesDisponibles);
    }
}
