package tn.esprit.eventservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventservice.dto.BehaviorDTO;
import tn.esprit.eventservice.entity.*;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.repository.UserBehaviorRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BehaviorServiceTest {

    @Mock
    private UserBehaviorRepository userBehaviorRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private BehaviorService behaviorService;

    private Event buildEvent(Long id) {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Tech");

        Event e = new Event();
        e.setId(id);
        e.setName("Conference");
        e.setLocation("Tunis");
        e.setStartDate(LocalDateTime.now().plusDays(1));
        e.setEndDate(LocalDateTime.now().plusDays(2));
        e.setMaxCapacity(100);
        e.setCurrentParticipants(0);
        e.setStatus(EventStatus.APPROVED);
        e.setCategory(cat);
        e.setUserId(1L);
        e.setPrice(BigDecimal.ZERO);
        e.setEventType(EventType.OUTDOOR);
        return e;
    }

    @Test
    @DisplayName("save_shouldSaveBehavior_whenDtoIsValid")
    void save_shouldSaveBehavior_whenDtoIsValid() {
        // Given
        BehaviorDTO dto = BehaviorDTO.builder()
                .userId(1L).eventId(10L).categoryId(2L)
                .actionType("VIEW").lat(36.8).lng(10.1)
                .build();
        given(eventRepository.findById(10L)).willReturn(Optional.of(buildEvent(10L)));

        // When
        behaviorService.save(dto);

        // Then
        verify(userBehaviorRepository).save(any(UserBehavior.class));
    }

    @Test
    @DisplayName("save_shouldUseCategoryFromEvent_whenCategoryIdIsNull")
    void save_shouldUseCategoryFromEvent_whenCategoryIdIsNull() {
        // Given
        BehaviorDTO dto = BehaviorDTO.builder()
                .userId(1L).eventId(10L).categoryId(null)
                .actionType("PARTICIPATE")
                .build();
        given(eventRepository.findById(10L)).willReturn(Optional.of(buildEvent(10L)));

        // When
        behaviorService.save(dto);

        // Then
        verify(userBehaviorRepository).save(any(UserBehavior.class));
    }

    @Test
    @DisplayName("save_shouldThrowIllegalArgument_whenUserIdIsNull")
    void save_shouldThrowIllegalArgument_whenUserIdIsNull() {
        // Given
        BehaviorDTO dto = BehaviorDTO.builder()
                .userId(null).eventId(10L).actionType("VIEW")
                .build();

        // When + Then
        assertThatThrownBy(() -> behaviorService.save(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId, eventId and actionType are required");
    }

    @Test
    @DisplayName("save_shouldThrowIllegalArgument_whenEventIdIsNull")
    void save_shouldThrowIllegalArgument_whenEventIdIsNull() {
        // Given
        BehaviorDTO dto = BehaviorDTO.builder()
                .userId(1L).eventId(null).actionType("VIEW")
                .build();

        // When + Then
        assertThatThrownBy(() -> behaviorService.save(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("save_shouldThrowIllegalArgument_whenActionTypeIsUnsupported")
    void save_shouldThrowIllegalArgument_whenActionTypeIsUnsupported() {
        // Given
        BehaviorDTO dto = BehaviorDTO.builder()
                .userId(1L).eventId(10L).actionType("INVALID_ACTION")
                .build();

        // When + Then
        assertThatThrownBy(() -> behaviorService.save(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported actionType");
    }

    @Test
    @DisplayName("save_shouldThrowRuntime_whenEventNotFound")
    void save_shouldThrowRuntime_whenEventNotFound() {
        // Given
        BehaviorDTO dto = BehaviorDTO.builder()
                .userId(1L).eventId(999L).actionType("VIEW")
                .build();
        given(eventRepository.findById(999L)).willReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> behaviorService.save(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Event introuvable");
    }

    @Test
    @DisplayName("save_shouldNormalizeActionType_whenLowerCase")
    void save_shouldNormalizeActionType_whenLowerCase() {
        // Given
        BehaviorDTO dto = BehaviorDTO.builder()
                .userId(1L).eventId(10L).actionType("view")
                .build();
        given(eventRepository.findById(10L)).willReturn(Optional.of(buildEvent(10L)));

        // When
        behaviorService.save(dto);

        // Then
        verify(userBehaviorRepository).save(any(UserBehavior.class));
    }
}