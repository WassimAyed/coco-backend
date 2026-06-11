package tn.esprit.eventservice.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EventCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventCleanupScheduler.class);

    private final EventRepository eventRepository;

    public EventCleanupScheduler(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void deleteFinishedEvents() {
        LocalDateTime now = LocalDateTime.now();

        List<Event> finishedEvents = eventRepository.findByEndDateBefore(now);

        if (finishedEvents.isEmpty()) {
            log.info("Scheduler: aucun événement terminé trouvé.");
            return;
        }

        eventRepository.deleteAll(finishedEvents);
        log.info("Scheduler: {} événement(s) terminé(s) supprimé(s) à {}", finishedEvents.size(), now);
    }
}