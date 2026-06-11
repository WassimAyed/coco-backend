package tn.esprit.eventservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventservice.dto.EventDTO;
import tn.esprit.eventservice.entity.Category;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventStatus;
import tn.esprit.eventservice.entity.EventType;
import tn.esprit.eventservice.exception.BusinessException;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.CategoryRepository;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.repository.ParticipantRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private GeocodingService geocodingService;

    @Mock
    private WeatherService weatherService;

    @Mock
    private FlaskService flaskService;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    @DisplayName("createEvent_shouldSaveEvent_whenPayloadIsValid")
    void createEvent_shouldSaveEvent_whenPayloadIsValid() {
        // Given
        EventDTO dto = buildEventDto();
        Category category = Category.builder().id(7L).name("Tech").build();
        Event persisted = buildEventEntity(10L, category);
        persisted.setPredictedParticipants(120);

        given(categoryRepository.findById(7L)).willReturn(Optional.of(category));
        given(geocodingService.getCoordinates("Tunis")).willReturn(null);
        given(eventRepository.save(any(Event.class))).willReturn(persisted);

        // When
        EventDTO result = eventService.createEvent(dto);

        // Then
        assertThat(result.getId()).isEqualTo(10L);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("createEvent_shouldThrowBusinessException_whenEndDateBeforeStartDate")
    void createEvent_shouldThrowBusinessException_whenEndDateBeforeStartDate() {
        // Given
        EventDTO dto = buildEventDto();
        dto.setEndDate(dto.getStartDate().minusHours(1));

        // When + Then
        assertThatThrownBy(() -> eventService.createEvent(dto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("getEventById_shouldThrowResourceNotFound_whenMissingEvent")
    void getEventById_shouldThrowResourceNotFound_whenMissingEvent() {
        // Given
        given(eventRepository.findById(404L)).willReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> eventService.getEventById(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateStatus_shouldUpdateStatus_whenEventExists")
    void updateStatus_shouldUpdateStatus_whenEventExists() {
        // Given
        Event event = buildEventEntity(5L, Category.builder().id(2L).name("Music").build());
        event.setStatus(EventStatus.PENDING);
        given(eventRepository.findById(5L)).willReturn(Optional.of(event));
        given(eventRepository.save(any(Event.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        EventDTO result = eventService.updateStatus(5L, EventStatus.ACCEPTED);

        // Then
        assertThat(result.getStatus()).isEqualTo(EventStatus.ACCEPTED);
    }

    private EventDTO buildEventDto() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate = startDate.plusHours(3);

        EventDTO dto = new EventDTO();
        dto.setName("Conference");
        dto.setLocation("Tunis");
        dto.setDescription("Tech event");
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setMaxCapacity(100);
        dto.setCurrentParticipants(0);
        dto.setUserId(11L);
        dto.setStatus(EventStatus.PENDING);
        dto.setCategoryId(7L);
        dto.setEventType(EventType.INDOOR);
        dto.setPrice(BigDecimal.valueOf(20));
        return dto;
    }

    private Event buildEventEntity(Long id, Category category) {
        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate = startDate.plusHours(3);

        Event event = new Event();
        event.setId(id);
        event.setName("Conference");
        event.setLocation("Tunis");
        event.setDescription("Tech event");
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setMaxCapacity(100);
        event.setCurrentParticipants(0);
        event.setUserId(11L);
        event.setStatus(EventStatus.PENDING);
        event.setCategory(category);
        event.setEventType(EventType.INDOOR);
        event.setPrice(BigDecimal.valueOf(20));
        return event;
    }
}
