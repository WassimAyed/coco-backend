package tn.esprit.eventservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tn.esprit.eventservice.dto.EventDTO;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AdminNotificationServiceTest {

    private AdminNotificationService adminNotificationService;

    @BeforeEach
    void setUp() {
        adminNotificationService = new AdminNotificationService();
    }

    @Test
    @DisplayName("subscribe_shouldReturnEmitter_andAddItToList")
    void subscribe_shouldReturnEmitter_andAddItToList() throws Exception {
        SseEmitter emitter = adminNotificationService.subscribe();

        assertThat(emitter).isNotNull();
        assertThat(getEmitters()).hasSize(1);
    }

    @Test
    @DisplayName("subscribe_shouldSendConnectedEvent_onSubscribe")
    void subscribe_shouldSendConnectedEvent_onSubscribe() {
        // No exception should be thrown
        SseEmitter emitter = adminNotificationService.subscribe();
        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("subscribe_multipleSubscribers_shouldAllBeRegistered")
    void subscribe_multipleSubscribers_shouldAllBeRegistered() throws Exception {
        adminNotificationService.subscribe();
        adminNotificationService.subscribe();
        adminNotificationService.subscribe();

        assertThat(getEmitters()).hasSize(3);
    }

    @Test
    @DisplayName("publishEventCreated_shouldSendToAllEmitters_whenValid")
    void publishEventCreated_shouldSendToAllEmitters_whenValid() throws Exception {
        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);

        injectEmitters(new CopyOnWriteArrayList<>(List.of(emitter1, emitter2)));

        EventDTO dto = new EventDTO();
        dto.setId(1L);
        dto.setName("Test Event");

        adminNotificationService.publishEventCreated(dto);

        verify(emitter1).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("publishEventCreated_shouldRemoveDeadEmitter_whenIOExceptionThrown")
    void publishEventCreated_shouldRemoveDeadEmitter_whenIOExceptionThrown() throws Exception {
        SseEmitter deadEmitter = mock(SseEmitter.class);
        doThrow(new IOException("broken pipe"))
                .when(deadEmitter).send(any(SseEmitter.SseEventBuilder.class));

        injectEmitters(new CopyOnWriteArrayList<>(List.of(deadEmitter)));

        EventDTO dto = new EventDTO();
        dto.setId(2L);
        dto.setName("Dead Emitter Event");

        adminNotificationService.publishEventCreated(dto);

        assertThat(getEmitters()).doesNotContain(deadEmitter);
    }

    @Test
    @DisplayName("publishEventCreated_shouldRemoveDeadEmitter_whenIllegalStateExceptionThrown")
    void publishEventCreated_shouldRemoveDeadEmitter_whenIllegalStateExceptionThrown() throws Exception {
        SseEmitter deadEmitter = mock(SseEmitter.class);
        doThrow(new IllegalStateException("emitter already completed"))
                .when(deadEmitter).send(any(SseEmitter.SseEventBuilder.class));

        injectEmitters(new CopyOnWriteArrayList<>(List.of(deadEmitter)));

        EventDTO dto = new EventDTO();
        dto.setId(3L);

        adminNotificationService.publishEventCreated(dto);

        assertThat(getEmitters()).doesNotContain(deadEmitter);
    }

    @Test
    @DisplayName("publishEventCreated_shouldKeepAliveEmitters_whenSendSucceeds")
    void publishEventCreated_shouldKeepAliveEmitters_whenSendSucceeds() throws Exception {
        SseEmitter goodEmitter = mock(SseEmitter.class);
        injectEmitters(new CopyOnWriteArrayList<>(List.of(goodEmitter)));

        EventDTO dto = new EventDTO();
        dto.setId(4L);

        adminNotificationService.publishEventCreated(dto);

        assertThat(getEmitters()).contains(goodEmitter);
    }

    // ── Reflection helpers ──────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<SseEmitter> getEmitters() throws Exception {
        Field field = AdminNotificationService.class.getDeclaredField("emitters");
        field.setAccessible(true);
        return (List<SseEmitter>) field.get(adminNotificationService);
    }

    private void injectEmitters(List<SseEmitter> list) throws Exception {
        Field field = AdminNotificationService.class.getDeclaredField("emitters");
        field.setAccessible(true);
        field.set(adminNotificationService, list);
    }
}