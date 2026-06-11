package tn.esprit.covoiturageservice.Service;

import tn.esprit.covoiturageservice.Entity.CovoiturageSchedule;

import java.util.List;

public interface ICovoiturageScheduleService {
    CovoiturageSchedule add(CovoiturageSchedule schedule);
    CovoiturageSchedule update(CovoiturageSchedule schedule);
    CovoiturageSchedule getById(Long id);
    List<CovoiturageSchedule> getAll();
    List<CovoiturageSchedule> getByDriver(Long idDriver);
    void delete(Long id);
    CovoiturageSchedule toggleActive(Long id);
    int generateDueCovoiturages();
}
