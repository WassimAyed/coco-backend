package tn.esprit.eventservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tn.esprit.eventservice.dto.PredictionRequest;
import tn.esprit.eventservice.dto.PredictionResponse;
import tn.esprit.eventservice.dto.EventDTO;
import tn.esprit.eventservice.entity.Category;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventStatus;
import tn.esprit.eventservice.exception.BusinessException;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.CategoryRepository;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.repository.ParticipantRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class EventServiceImpl implements IEventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);
    private static final String EVENT_NOT_FOUND_MSG = "Événement introuvable : ";


    private final FlaskService flaskService;

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final CategoryRepository categoryRepository;
    private final GeocodingService geocodingService;
    private final WeatherService weatherService;

    public EventServiceImpl(EventRepository eventRepository,
                            ParticipantRepository participantRepository,
                            CategoryRepository categoryRepository,
                            GeocodingService geocodingService,
                            WeatherService weatherService,
                            FlaskService flaskService) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.categoryRepository = categoryRepository;
        this.geocodingService = geocodingService;
        this.weatherService = weatherService;
        this.flaskService = flaskService;
    }

    // ---------- Mapping ----------

    private EventDTO toDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setLocation(event.getLocation());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setDescription(event.getDescription());
        dto.setImageUrl(event.getImageUrl());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setMaxCapacity(event.getMaxCapacity());
        dto.setCurrentParticipants(event.getCurrentParticipants());
        dto.setUserId(event.getUserId());
        dto.setStatus(event.getStatus());
        dto.setEventType(event.getEventType());
        dto.setPrice(event.getPrice());
        dto.setTemperature(event.getTemperature());
        dto.setPrecipitationMm(event.getPrecipitationMm());
        dto.setWindSpeedKmh(event.getWindSpeedKmh());
        dto.setWeatherCode(event.getWeatherCode());
        dto.setWeatherLabel(event.getWeatherLabel());
        dto.setPredictedParticipants(event.getPredictedParticipants());
        if (event.getCategory() != null)
            dto.setCategoryId(event.getCategory().getId());
        return dto;
    }

    private Event toEntity(EventDTO dto) {
        Event event = new Event();
        event.setName(dto.getName());
        event.setLocation(dto.getLocation());
        event.setLatitude(dto.getLatitude());
        event.setLongitude(dto.getLongitude());
        event.setDescription(dto.getDescription());
        event.setImageUrl(dto.getImageUrl());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setMaxCapacity(dto.getMaxCapacity());
        event.setCurrentParticipants(dto.getCurrentParticipants() != null ? dto.getCurrentParticipants() : 0);
        event.setEventType(dto.getEventType());
        event.setPrice(dto.getPrice());
        event.setUserId(dto.getUserId());
        event.setStatus(dto.getStatus());
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + dto.getCategoryId()));
            event.setCategory(category);
        }
        return event;
    }

    // ---------- CRUD ----------

    @Override
    public EventDTO createEvent(EventDTO dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate()))
            throw new BusinessException("La date de fin doit être après la date de début");

        Event event = toEntity(dto);
        event.setStatus(dto.getStatus() != null ? dto.getStatus() : EventStatus.PENDING);

        // ── Géocodage automatique via Nominatim (OpenStreetMap) ──
        // On utilise l'adresse saisie dans le champ "location"
        enrichGeoAndWeather(event, dto.getLocation());

        return toDTO(eventRepository.save(event));
    }

    @Override
    public EventDTO updateEvent(Long id, EventDTO dto) {
        Event existing = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_MSG + id));

        if (isLockedStatus(existing.getStatus())) {
            throw new BusinessException("L'événement ne peut plus être modifié après rejet");
        }

        existing.setName(dto.getName());
        existing.setLocation(dto.getLocation());
        existing.setLatitude(dto.getLatitude());
        existing.setLongitude(dto.getLongitude());
        existing.setDescription(dto.getDescription());
        existing.setImageUrl(dto.getImageUrl());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setMaxCapacity(dto.getMaxCapacity());
        existing.setPrice(dto.getPrice());
        existing.setEventType(dto.getEventType());
        if (dto.getUserId() != null) {
            existing.setUserId(dto.getUserId());
        }
        existing.setStatus(dto.getStatus());
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + dto.getCategoryId()));
            existing.setCategory(category);
        }

        enrichGeoAndWeather(existing, dto.getLocation());
        return toDTO(eventRepository.save(existing));
    }

    @Override
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_MSG + id));
        eventRepository.delete(event);
    }

    @Override
    public EventDTO getEventById(Long id) {
        return toDTO(eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_MSG + id)));
    }

    @Override
    public List<EventDTO> getAllEvents() {
        return eventRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public Page<EventDTO> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public List<EventDTO> getAllEvents(Long userId) {
        return eventRepository.findAll().stream()
            .filter(event -> isPublicStatus(event.getStatus())
                        || (event.getUserId() != null && event.getUserId().equals(userId)))
                .map(this::toDTO)
                .toList();
    }

    @Override
    public Page<EventDTO> getAllEvents(Long userId, Pageable pageable) {
        List<EventDTO> filtered = eventRepository.findAll().stream()
                .filter(event -> isPublicStatus(event.getStatus())
                        || (event.getUserId() != null && event.getUserId().equals(userId)))
                .sorted(sortByStartDateDesc())
                .map(this::toDTO)
            .toList();
        return paginateList(filtered, pageable);
    }

    @Override
    public Page<EventDTO> getEventsCreatedByUser(Long userId, Pageable pageable) {
        return eventRepository.findByUserId(userId, pageable).map(this::toDTO);
    }

    @Override
    public Page<EventDTO> getParticipatedEvents(String email, String phone, Pageable pageable) {
        Set<Long> eventIds = new LinkedHashSet<>();

        if (email != null && !email.isBlank()) {
            eventIds.addAll(participantRepository.findDistinctEventIdsByEmail(email.trim().toLowerCase()));
        }

        if (phone != null && !phone.isBlank()) {
            eventIds.addAll(participantRepository.findDistinctEventIdsByPhone(phone.trim()));
        }

        if (eventIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<EventDTO> participatedEvents = eventRepository.findAllById(eventIds).stream()
                .sorted(sortByStartDateDesc())
                .map(this::toDTO)
            .toList();

        return paginateList(participatedEvents, pageable);
    }

    @Override
    public List<EventDTO> getEventsByStatus(EventStatus status) {
        return eventRepository.findByStatus(status).stream().map(this::toDTO).toList();
    }

    @Override
    public Page<EventDTO> getEventsByStatus(EventStatus status, Pageable pageable) {
        return eventRepository.findByStatus(status, pageable).map(this::toDTO);
    }

    @Override
    public List<EventDTO> getEventsByCategory(Long categoryId) {
        return eventRepository.findByCategoryId(categoryId).stream().map(this::toDTO).toList();
    }

    @Override
    public Page<EventDTO> getEventsByCategory(Long categoryId, Pageable pageable) {
        return eventRepository.findByCategoryId(categoryId, pageable).map(this::toDTO);
    }

    @Override
    public List<EventDTO> searchEventsByName(String name) {
        return eventRepository.findByNameContainingIgnoreCase(name).stream().map(this::toDTO).toList();
    }

    @Override
    public Page<EventDTO> searchEventsByName(String name, Pageable pageable) {
        return eventRepository.findByNameContainingIgnoreCase(name, pageable).map(this::toDTO);
    }

    @Override
    public List<EventDTO> getAvailableEvents() {
        return eventRepository.findAvailableEvents().stream().map(this::toDTO).toList();
    }

    @Override
    public Page<EventDTO> getAvailableEvents(Pageable pageable) {
        return eventRepository.findAvailableEvents(pageable).map(this::toDTO);
    }

    @Override
    public List<EventDTO> getEventsByDateRange(LocalDateTime from, LocalDateTime to) {
        return eventRepository.findByDateRange(from, to).stream().map(this::toDTO).toList();
    }

    @Override
    public Page<EventDTO> getEventsByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return eventRepository.findByDateRange(from, to, pageable).map(this::toDTO);
    }

    // ---------- Géolocalisation ----------

    /**
     * Retourne les événements dans un rayon donné (km) autour d'un point GPS.
     * Utilise la formule de Haversine pour le calcul de distance.
     */
    @Override
    public List<EventDTO> getEventsNearby(Double lat, Double lng, Double radiusKm) {
        return eventRepository.findAll().stream()
                .filter(e -> e.getLatitude() != null && e.getLongitude() != null)
                .filter(e -> haversineKm(lat, lng, e.getLatitude(), e.getLongitude()) <= radiusKm)
                .map(this::toDTO)
                .toList();
    }

    @Override
    public Page<EventDTO> getEventsNearby(Double lat, Double lng, Double radiusKm, Pageable pageable) {
        List<EventDTO> nearbyEvents = eventRepository.findAll().stream()
                .filter(e -> e.getLatitude() != null && e.getLongitude() != null)
                .filter(e -> haversineKm(lat, lng, e.getLatitude(), e.getLongitude()) <= radiusKm)
                .sorted(sortByStartDateDesc())
                .map(this::toDTO)
            .toList();
        return paginateList(nearbyEvents, pageable);
    }

    /** Haversine formula — distance en km entre deux points GPS. */
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // ---------- Admin status management ----------

    @Override
    public EventDTO updateStatus(Long id, EventStatus status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_MSG + id));

        if (isLockedStatus(event.getStatus())) {
            throw new BusinessException("Le statut ne peut être modifié que si l'événement est en attente");
        }

        event.setStatus(status);
        return toDTO(eventRepository.save(event));
    }

    private void enrichGeoAndWeather(Event event, String location) {
        boolean hasCoords = event.getLatitude() != null && event.getLongitude() != null;

        if (!hasCoords && hasText(location)) {
            Map<String, String> geo = geocodingService.getCoordinates(location);
            if (geo != null) {
                String latStr = geo.get("lat");
                String lonStr = geo.get("lon");
                if (latStr != null && !latStr.isBlank() && lonStr != null && !lonStr.isBlank()) {
                    try {
                        event.setLatitude(Double.parseDouble(latStr.trim()));
                        event.setLongitude(Double.parseDouble(lonStr.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("Coordonnées invalides retournées par le géocodage : lat={}, lon={}", latStr, lonStr);
                    }
                } else {
                    log.warn("Géocodage : clés lat/lon absentes pour location='{}'. Map reçue : {}", location, geo);
                }
            } else {
                log.warn("Géocodage : aucun résultat pour location='{}'", location);
            }
        }

        if (event.getLatitude() == null || event.getLongitude() == null) {
            log.warn("Pas de coordonnées disponibles, météo ignorée pour : {}", event.getName());
            event.setPredictedParticipants(predictParticipants(event));
            return;
        }

        WeatherService.WeatherSnapshot weatherSnapshot = weatherService.getAverageDailyWeather(
                event.getLatitude(),
                event.getLongitude(),
                event.getStartDate() != null ? event.getStartDate().toLocalDate() : null,
                event.getEndDate() != null ? event.getEndDate().toLocalDate() : null
        );
        if (weatherSnapshot != null) {
            event.setTemperature(weatherSnapshot.getTemperature());
            event.setPrecipitationMm(weatherSnapshot.getPrecipitationMm());
            event.setWindSpeedKmh(weatherSnapshot.getWindSpeedKmh());
            event.setWeatherCode(weatherSnapshot.getWeatherCode());
            event.setWeatherLabel(weatherSnapshot.getWeatherLabel());
        }

        event.setPredictedParticipants(predictParticipants(event));
    }
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private Integer predictParticipants(Event event) {
        try {
            if (event.getCategory() == null || event.getStartDate() == null || event.getEndDate() == null) {
                return null;
            }

            PredictionRequest request = new PredictionRequest();
            request.setCategoryId(event.getCategory().getId());
            request.setPrice(event.getPrice() != null ? event.getPrice().doubleValue() : 0.0);
            request.setIsFree(event.getPrice() == null || event.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0 ? 1 : 0);
            request.setMaxCapacity(event.getMaxCapacity());
            request.setEventType(event.getEventType() != null ? event.getEventType().name() : null);

            java.time.LocalDate startDate = event.getStartDate().toLocalDate();
            request.setIsWeekend(startDate.getDayOfWeek().getValue() >= 6 ? 1 : 0);
            request.setIsHoliday(0);

            long durationDays = java.time.temporal.ChronoUnit.DAYS.between(
                    event.getStartDate().toLocalDate(),
                    event.getEndDate().toLocalDate()
            ) + 1;
            request.setDurationDays((int) Math.max(durationDays, 1));

            long daysUntilEvent = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), startDate);
            request.setDaysUntilEvent((int) Math.max(daysUntilEvent, 0));

            request.setTemperature(event.getTemperature() != null ? event.getTemperature() : 20.0);
            request.setPrecipitationMm(event.getPrecipitationMm() != null ? event.getPrecipitationMm() : 0.0);
            request.setWindSpeedKmh(event.getWindSpeedKmh() != null ? event.getWindSpeedKmh() : 0.0);

            String json = flaskService.callFlask(request);
            if (json == null) return null;
            ObjectMapper mapper = new ObjectMapper();
            PredictionResponse response = mapper.readValue(json, PredictionResponse.class);
            return response.getPredictedParticipants();
        } catch (Exception exception) {
            log.warn("Flask prediction service unavailable", exception);
            return null;
        }
    }

    private boolean isPublicStatus(EventStatus status) {
        return status == EventStatus.APPROVED || status == EventStatus.ACCEPTED;
    }

    // APRÈS
    private boolean isLockedStatus(EventStatus status) {
        return status == EventStatus.REJECTED || status == EventStatus.REFUSED;
    }

    private Comparator<Event> sortByStartDateDesc() {
        return Comparator.comparing(Event::getStartDate, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private Page<EventDTO> paginateList(List<EventDTO> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= items.size()) {
            return new PageImpl<>(List.of(), pageable, items.size());
        }

        int end = Math.min(start + pageable.getPageSize(), items.size());
        return new PageImpl<>(items.subList(start, end), pageable, items.size());
    }
}