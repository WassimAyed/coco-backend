package tn.esprit.covoiturageservice.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.covoiturageservice.Entity.Covoiturage;
import tn.esprit.covoiturageservice.Entity.CovoiturageSchedule;
import tn.esprit.covoiturageservice.Entity.CovoiturageSchedule.Frequency;
import tn.esprit.covoiturageservice.Repository.ICovoiturageRepository;
import tn.esprit.covoiturageservice.Repository.ICovoiturageScheduleRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CovoiturageScheduleServiceImp implements ICovoiturageScheduleService {

    private final ICovoiturageScheduleRepository scheduleRepository;
    private final ICovoiturageRepository covoiturageRepository;

    @Override
    public CovoiturageSchedule add(CovoiturageSchedule schedule) {
        schedule.setCreatedAt(LocalDateTime.now());
        if (schedule.getStartDate() == null) {
            schedule.setStartDate(LocalDate.now());
        }
        return scheduleRepository.save(schedule);
    }

    @Override
    public CovoiturageSchedule update(CovoiturageSchedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @Override
    public CovoiturageSchedule getById(Long id) {
        return scheduleRepository.findById(id).orElse(null);
    }

    @Override
    public List<CovoiturageSchedule> getAll() {
        return scheduleRepository.findAll();
    }

    @Override
    public List<CovoiturageSchedule> getByDriver(Long idDriver) {
        return scheduleRepository.findByIdDriver(idDriver);
    }

    @Override
    public void delete(Long id) {
        scheduleRepository.deleteById(id);
    }

    @Override
    public CovoiturageSchedule toggleActive(Long id) {
        CovoiturageSchedule s = scheduleRepository.findById(id).orElse(null);
        if (s == null) return null;
        s.setActive(!s.isActive());
        return scheduleRepository.save(s);
    }

    @Override
    public int generateDueCovoiturages() {
        LocalDate today = LocalDate.now();
        List<CovoiturageSchedule> active = scheduleRepository.findByActiveTrue();
        int generated = 0;

        for (CovoiturageSchedule s : active) {
            if (!isDueToday(s, today)) continue;
            if (s.getLastGeneratedDate() != null && !s.getLastGeneratedDate().isBefore(today)) continue;

            covoiturageRepository.save(buildCovoiturage(s, today));
            s.setLastGeneratedDate(today);
            scheduleRepository.save(s);
            generated++;
        }
        return generated;
    }

    private boolean isDueToday(CovoiturageSchedule s, LocalDate today) {
        if (s.getStartDate() != null && today.isBefore(s.getStartDate())) return false;
        if (s.getEndDate() != null && today.isAfter(s.getEndDate())) return false;

        if (s.getFrequency() == Frequency.DAILY) return true;

        if (s.getFrequency() == Frequency.WEEKLY && s.getDaysOfWeek() != null) {
            Set<String> days = Arrays.stream(s.getDaysOfWeek().split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());
            return days.contains(toShortCode(today.getDayOfWeek()));
        }
        return false;
    }

    private String toShortCode(DayOfWeek d) {
        return switch (d) {
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };
    }

    private Covoiturage buildCovoiturage(CovoiturageSchedule s, LocalDate day) {
        Covoiturage c = new Covoiturage();
        c.setPointDepart(s.getPointDepart());
        c.setPointArrivee(s.getPointArrivee());
        c.setDateDepart(LocalDateTime.of(day, s.getHeureDepart()));
        c.setNombrePlaces(s.getNombrePlaces());
        c.setPlacesDisponibles(s.getNombrePlaces());
        c.setLattitudeDepart(s.getLattitudeDepart());
        c.setLongitudeDepart(s.getLongitudeDepart());
        c.setLatitudeArrivee(s.getLatitudeArrivee());
        c.setLongitudeArrivee(s.getLongitudeArrivee());
        c.setPrixParPassager(s.getPrixParPassager());
        c.setDistance(s.getDistance());
        c.setDureeEstimee(s.getDureeEstimee());
        c.setIdDriver(s.getIdDriver());
        c.setVehicleId(s.getVehicleId());
        return c;
    }
}
