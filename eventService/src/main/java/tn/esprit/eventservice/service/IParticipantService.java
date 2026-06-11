package tn.esprit.eventservice.service;

import tn.esprit.eventservice.dto.ParticipantDTO;
import java.util.List;

public interface IParticipantService {
    ParticipantDTO registerParticipant(ParticipantDTO dto);
    void unregisterParticipant(Long id);
    ParticipantDTO getParticipantById(Long id);
    List<ParticipantDTO> getParticipantsByEvent(Long eventId);
    long countParticipantsByEvent(Long eventId);
}