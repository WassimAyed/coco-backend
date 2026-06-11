package tn.esprit.covoiturageservice.Scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprit.covoiturageservice.Service.ICovoiturageScheduleService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CovoiturageScheduleJob {

    private final ICovoiturageScheduleService scheduleService;

    // Runs on app startup after 30s, then every 5 second
    @Scheduled(initialDelay = 30_000, fixedDelay = 5_000)
    public void run() {
        int generated = scheduleService.generateDueCovoiturages();
        if (generated > 0) {
            log.info("CovoiturageScheduleJob: generated {} covoiturage(s) from templates", generated);
        }
    }
}
