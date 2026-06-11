package tn.esprit.eventservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventservice.dto.ParticipantDTO;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventStatus;
import tn.esprit.eventservice.entity.Participant;
import tn.esprit.eventservice.exception.BusinessException;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.repository.ParticipantRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceImplTest {

    @Mock ParticipantRepository participantRepository;
    @Mock EventRepository eventRepository;
    @Mock SmsService smsService;
    @InjectMocks ParticipantServiceImpl participantService;

    private Event event;
    private Participant participant;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1L);
        event.setName("Tech Summit");
        event.setUserId(10L);
        event.setMaxCapacity(100);
        event.setCurrentParticipants(0);
        event.setStatus(EventStatus.ACCEPTED);

        participant = new Participant();
        participant.setId(1L);
        participant.setFullName("Ali Ben Ali");
        participant.setEmail("ali@test.com");
        participant.setPhone("55123456");
        participant.setEvent(event);
    }

    @Test
    void shouldRegisterParticipant() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setEventId(1L);
        dto.setUserId(99L);
        dto.setFullName("Ali Ben Ali");
        dto.setEmail("ali@test.com");
        dto.setPhone("55123456");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.existsByEmailAndEventId("ali@test.com", 1L)).thenReturn(false);
        when(participantRepository.save(any())).thenReturn(participant);
        when(eventRepository.save(any())).thenReturn(event);

        ParticipantDTO result = participantService.registerParticipant(dto);

        assertNotNull(result);
        assertEquals("Ali Ben Ali", result.getFullName());
        verify(participantRepository).save(any());
    }

    @Test
    void shouldThrowWhenEventCreatorTriesToParticipate() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setEventId(1L);
        dto.setUserId(10L); // même userId que le créateur
        dto.setEmail("creator@test.com");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(BusinessException.class, () -> participantService.registerParticipant(dto));
    }

    @Test
    void shouldThrowWhenEmailAlreadyRegistered() {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setEventId(1L);
        dto.setUserId(99L);
        dto.setEmail("ali@test.com");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.existsByEmailAndEventId("ali@test.com", 1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> participantService.registerParticipant(dto));
    }

    @Test
    void shouldThrowWhenEventIsFull() {
        event.setCurrentParticipants(100); // capacité max atteinte

        ParticipantDTO dto = new ParticipantDTO();
        dto.setEventId(1L);
        dto.setUserId(99L);
        dto.setEmail("nouveau@test.com");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.existsByEmailAndEventId(any(), any())).thenReturn(false);

        assertThrows(BusinessException.class, () -> participantService.registerParticipant(dto));
    }

    @Test
    void shouldUnregisterParticipant() {
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        when(eventRepository.save(any())).thenReturn(event);

        participantService.unregisterParticipant(1L);

        verify(participantRepository).deleteById(1L);
        verify(eventRepository).save(any());
    }

    @Test
    void shouldThrowWhenUnregisteringNonExistentParticipant() {
        when(participantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> participantService.unregisterParticipant(99L));
    }

    @Test
    void shouldGetParticipantsByEvent() {
        when(participantRepository.findByEventId(1L)).thenReturn(List.of(participant));

        List<ParticipantDTO> result = participantService.getParticipantsByEvent(1L);

        assertEquals(1, result.size());
        assertEquals("Ali Ben Ali", result.get(0).getFullName());
    }

    @Test
    void shouldCountParticipantsByEvent() {
        when(participantRepository.countByEventId(1L)).thenReturn(5L);

        long count = participantService.countParticipantsByEvent(1L);

        assertEquals(5L, count);
    }
}