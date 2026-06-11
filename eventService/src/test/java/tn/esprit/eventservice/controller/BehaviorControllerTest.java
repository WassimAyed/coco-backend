package tn.esprit.eventservice.controller;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tn.esprit.eventservice.dto.BehaviorDTO;
import tn.esprit.eventservice.service.BehaviorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BehaviorControllerTest {

    @Mock
    private BehaviorService behaviorService;

    @InjectMocks
    private BehaviorController behaviorController;

    @Test
    @DisplayName("recordBehavior_shouldReturn200_whenDtoIsValid")
    void recordBehavior_shouldReturn200_whenDtoIsValid() {
        // Given
        BehaviorDTO dto = BehaviorDTO.builder()
                .userId(1L)
                .eventId(10L)
                .categoryId(2L)
                .actionType("VIEW")
                .lat(36.8)
                .lng(10.1)
                .build();

        // When
        ResponseEntity<Void> result = behaviorController.recordBehavior(dto);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(behaviorService).save(dto);
    }

    @Test
    @DisplayName("recordBehavior_shouldCallSave_withCorrectDto")
    void recordBehavior_shouldCallSave_withCorrectDto() {
        // Given
        BehaviorDTO dto = BehaviorDTO.builder()
                .userId(5L)
                .eventId(20L)
                .actionType("PARTICIPATE")
                .build();

        // When
        behaviorController.recordBehavior(dto);

        // Then
        verify(behaviorService).save(dto);
    }
}