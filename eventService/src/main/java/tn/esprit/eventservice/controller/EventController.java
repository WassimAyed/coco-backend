package tn.esprit.eventservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tn.esprit.eventservice.dto.EventDTO;
import tn.esprit.eventservice.entity.EventStatus;
import tn.esprit.eventservice.scheduler.EventCleanupScheduler;
import tn.esprit.eventservice.service.AdminNotificationService;
import tn.esprit.eventservice.service.IEventService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Gestion des événements avec géolocalisation OpenStreetMap (Nominatim)")
public class EventController {

    private static final String START_DATE_SORT = "startDate";

    private final IEventService eventService;
    private final AdminNotificationService adminNotificationService;

    public EventController(IEventService eventService, AdminNotificationService adminNotificationService) {
        this.eventService = eventService;
        this.adminNotificationService = adminNotificationService;
    }

    @PostMapping
    @Operation(
        summary = "Créer un événement",
        description = "Crée un événement. Le champ 'location' (adresse texte) est automatiquement "
                    + "géocodé via l'API Nominatim d'OpenStreetMap pour remplir latitude et longitude. "
                    + "Ensuite, la météo du jour de l'événement est récupérée via Open-Meteo (daily forecast) "
                    + "et stockée dans l'événement (temperature, weatherCode, weatherLabel)."
    )
    public ResponseEntity<EventDTO> create(@Valid @RequestBody EventDTO dto) {
        EventDTO created = eventService.createEvent(dto);
        try {
            adminNotificationService.publishEventCreated(created);
        } catch (Exception e) {
            // notification échouée = pas grave, l'event est bien créé
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping(value = "/notifications/admin-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "Flux SSE temps reel pour les admins",
        description = "Notifie les admins connectes a chaque creation d'evenement."
    )
    public SseEmitter streamAdminNotifications() {
        return adminNotificationService.subscribe();
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Mettre à jour un événement",
        description = "Met à jour un événement existant uniquement si son statut est PENDING. Vous pouvez fournir "
                    + "manuellement latitude/longitude ou laisser le système les recalculer depuis location."
    )
    public ResponseEntity<EventDTO> update(@PathVariable("id") Long id, @Valid @RequestBody EventDTO dto) {
        EventStatus currentStatus = eventService.getEventById(id).getStatus();
        if (currentStatus == EventStatus.REFUSED || currentStatus == EventStatus.REJECTED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(eventService.updateEvent(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un événement")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un événement par ID",
               description = "Retourne l'événement avec ses coordonnées GPS (latitude, longitude).")
    public ResponseEntity<EventDTO> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping
    @Operation(summary = "Lister tous les événements")
    public ResponseEntity<Page<EventDTO>> getAll(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(START_DATE_SORT).descending());
        if (userId == null) {
            return ResponseEntity.ok(eventService.getAllEvents(pageable));
        }
        return ResponseEntity.ok(eventService.getAllEvents(userId, pageable));
    }


    @GetMapping("/status/{status}")
    @Operation(summary = "Filtrer par statut")
    public ResponseEntity<Page<EventDTO>> getByStatus(
            @PathVariable("status") EventStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(START_DATE_SORT).descending());
        return ResponseEntity.ok(eventService.getEventsByStatus(status, pageable));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Filtrer par catégorie")
    public ResponseEntity<Page<EventDTO>> getByCategory(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(START_DATE_SORT).descending());
        return ResponseEntity.ok(eventService.getEventsByCategory(categoryId, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher par nom")
    public ResponseEntity<Page<EventDTO>> search(
            @RequestParam("name") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(START_DATE_SORT).descending());
        return ResponseEntity.ok(eventService.searchEventsByName(name, pageable));
    }

    @GetMapping("/available")
    @Operation(summary = "Événements disponibles (places restantes)")
    public ResponseEntity<Page<EventDTO>> getAvailable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(START_DATE_SORT).descending());
        return ResponseEntity.ok(eventService.getAvailableEvents(pageable));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Filtrer par plage de dates")
    public ResponseEntity<Page<EventDTO>> getByDateRange(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(START_DATE_SORT).descending());
        return ResponseEntity.ok(eventService.getEventsByDateRange(from, to, pageable));
    }

    @GetMapping("/nearby")
    @Operation(
        summary = "🗺️ Événements à proximité (Haversine)",
        description = "Retourne les événements dans un rayon donné autour d'un point GPS. "
                    + "Utilise la formule de Haversine. Seuls les événements avec des coordonnées sont inclus."
    )
    public ResponseEntity<Page<EventDTO>> getNearby(
            @Parameter(description = "Latitude du point de référence", example = "36.8665") @RequestParam("lat") Double lat,
            @Parameter(description = "Longitude du point de référence", example = "10.1647") @RequestParam("lng") Double lng,
            @Parameter(description = "Rayon de recherche en kilomètres", example = "10.0") @RequestParam("radius") Double radius,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(START_DATE_SORT).descending());
        return ResponseEntity.ok(eventService.getEventsNearby(lat, lng, radius, pageable));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'un événement (admin)")
    public ResponseEntity<EventDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam EventStatus status) {
        return ResponseEntity.ok(eventService.updateStatus(id, status));
    }

    @GetMapping("/user")
    @Operation(summary = "Lister les événements visibles pour un utilisateur",
               description = "Retourne les événements APPROVED + les événements créés par l'utilisateur.")
    public ResponseEntity<Page<EventDTO>> getAllForUser(
            @RequestParam("userId") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(START_DATE_SORT).descending());
        return ResponseEntity.ok(eventService.getAllEvents(userId, pageable));
    }

    @GetMapping("/created-by/{userId}")
    @Operation(summary = "Lister les événements créés par un utilisateur")
    public ResponseEntity<Page<EventDTO>> getCreatedByUser(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(START_DATE_SORT).descending());
        return ResponseEntity.ok(eventService.getEventsCreatedByUser(userId, pageable));
    }

    @GetMapping("/participated")
    @Operation(summary = "Lister les événements auxquels un utilisateur participe")
    public ResponseEntity<Page<EventDTO>> getParticipatedEvents(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(START_DATE_SORT).descending());


        return ResponseEntity.ok(eventService.getParticipatedEvents(email, phone, pageable));
    }

    @RestController
    @RequestMapping("/api/admin")
    public class SchedulerController {

        private final EventCleanupScheduler scheduler;

        public SchedulerController(EventCleanupScheduler scheduler) {
            this.scheduler = scheduler;
        }

        @PostMapping("/trigger-cleanup")
        public ResponseEntity<String> triggerCleanup() {
            scheduler.deleteFinishedEvents();
            return ResponseEntity.ok("Cleanup exécuté manuellement");
        }
    }
}