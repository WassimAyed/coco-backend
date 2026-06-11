package tn.esprit.eventservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.eventservice.dto.ParticipantDTO;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.Participant;
import tn.esprit.eventservice.exception.BusinessException;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.repository.ParticipantRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParticipantServiceImpl implements IParticipantService {

    private static final Logger log = LoggerFactory.getLogger(ParticipantServiceImpl.class);

        private final ParticipantRepository participantRepository;
        private final EventRepository eventRepository;
        private final SmsService smsService;

    public ParticipantServiceImpl(ParticipantRepository participantRepository,
                                  EventRepository eventRepository,
                                  SmsService smsService) {
        this.participantRepository = participantRepository;
        this.eventRepository = eventRepository;
        this.smsService = smsService;
    }

        private ParticipantDTO toDTO(Participant p) {
            ParticipantDTO dto = new ParticipantDTO();
            dto.setId(p.getId());
            dto.setFullName(p.getFullName());
            dto.setEmail(p.getEmail());
            dto.setPhone(p.getPhone());
            dto.setRegistrationDate(p.getRegistrationDate());
            if (p.getEvent() != null)
                dto.setEventId(p.getEvent().getId());
            return dto;
        }

        @Override
        public ParticipantDTO registerParticipant(ParticipantDTO dto) {
            Event event = eventRepository.findById(dto.getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable : " + dto.getEventId()));

            if (dto.getUserId() != null && event.getUserId() != null
                    && dto.getUserId().equals(event.getUserId()))
                throw new BusinessException("Le createur ne peut pas participer a son propre evenement");

            if (participantRepository.existsByEmailAndEventId(dto.getEmail(), dto.getEventId()))
                throw new BusinessException("Cet email est déjà inscrit à cet événement");

            if (event.getCurrentParticipants() >= event.getMaxCapacity())
                throw new BusinessException("Capacité maximale atteinte pour cet événement");

            Participant participant = new Participant();
            participant.setFullName(dto.getFullName());
            participant.setEmail(dto.getEmail());
            participant.setPhone(dto.getPhone());
            participant.setRegistrationDate(LocalDateTime.now());
            participant.setEvent(event);

            event.setCurrentParticipants(event.getCurrentParticipants() + 1);
            eventRepository.save(event);

            Participant saved = participantRepository.save(participant);

            // ✅ Envoi SMS de confirmation
            if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
                try {
                    String message = String.format(
                            "Bonjour %s ! Votre inscription à l'événement \"%s\" le %s à %s est confirmée. À bientôt !",
                            participant.getFullName(),
                            event.getName(),
                            event.getStartDate().toLocalDate(),
                            event.getLocation()
                    );
                    smsService.sendSms("+216" + dto.getPhone(), message);
                } catch (Exception e) {
                    log.warn("SMS non envoyé pour participant {}", participant.getId(), e);
                }
            }

            return toDTO(saved);
        }

        @Override
        public void unregisterParticipant(Long id) {
            Participant participant = participantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Participant introuvable : " + id));

            Event event = participant.getEvent();
            event.setCurrentParticipants(event.getCurrentParticipants() - 1);
            eventRepository.save(event);

            participantRepository.deleteById(id);
        }

        @Override
        public ParticipantDTO getParticipantById(Long id) {
            return toDTO(participantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Participant introuvable : " + id)));
        }

        @Override
    public List<ParticipantDTO> getParticipantsByEvent(Long eventId) {
        return participantRepository.findByEventId(eventId).stream().map(this::toDTO).toList();
    }

        @Override
    public long countParticipantsByEvent(Long eventId) {
        return participantRepository.countByEventId(eventId);
    }
}