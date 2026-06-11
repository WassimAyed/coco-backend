package tn.esprit.covoiturageservice.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.covoiturageservice.Entity.CovoiturageSchedule;
import tn.esprit.covoiturageservice.Service.ICovoiturageScheduleService;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/covoiturage/schedule")
public class CovoiturageScheduleController {

    private final ICovoiturageScheduleService scheduleService;

    @PostMapping(value = "/add", consumes = "application/json", produces = "application/json")
    public CovoiturageSchedule add(@Valid @RequestBody CovoiturageSchedule schedule) {
        return scheduleService.add(schedule);
    }

    @PutMapping(value = "/update", consumes = "application/json", produces = "application/json")
    public CovoiturageSchedule update(@Valid @RequestBody CovoiturageSchedule schedule) {
        return scheduleService.update(schedule);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CovoiturageSchedule> getById(@PathVariable Long id) {
        CovoiturageSchedule s = scheduleService.getById(id);
        return s == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(s);
    }

    @GetMapping("/all")
    public List<CovoiturageSchedule> getAll() {
        return scheduleService.getAll();
    }

    @GetMapping("/driver/{idDriver}")
    public List<CovoiturageSchedule> getByDriver(@PathVariable Long idDriver) {
        return scheduleService.getByDriver(idDriver);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        scheduleService.delete(id);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<CovoiturageSchedule> toggle(@PathVariable Long id) {
        CovoiturageSchedule s = scheduleService.toggleActive(id);
        return s == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(s);
    }

    @PostMapping("/run-now")
    public Map<String, Integer> runNow() {
        return Map.of("generated", scheduleService.generateDueCovoiturages());
    }
}
