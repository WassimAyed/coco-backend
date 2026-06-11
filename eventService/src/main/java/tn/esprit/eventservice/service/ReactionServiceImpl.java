package tn.esprit.eventservice.service;

import org.springframework.stereotype.Service;
import tn.esprit.eventservice.dto.ReactionDTO;
import tn.esprit.eventservice.dto.ReactionSummaryDTO;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.Reaction;
import tn.esprit.eventservice.entity.ReactionType;
import tn.esprit.eventservice.exception.BusinessException;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.repository.ReactionRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReactionServiceImpl implements IReactionService {

    private final ReactionRepository reactionRepository;
    private final EventRepository eventRepository;

    public ReactionServiceImpl(ReactionRepository reactionRepository,
                               EventRepository eventRepository) {
        this.reactionRepository = reactionRepository;
        this.eventRepository = eventRepository;
    }

    private ReactionDTO toDTO(Reaction r) {
        ReactionDTO dto = new ReactionDTO();
        dto.setId(r.getId());
        dto.setType(r.getType());
        dto.setAuthorName(r.getAuthorName());
        dto.setAuthorEmail(r.getAuthorEmail());
        dto.setEventId(r.getEvent().getId());
        return dto;
    }

    @Override
    public ReactionDTO addOrUpdateReaction(ReactionDTO dto) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Événement introuvable : " + dto.getEventId()));

        // Si déjà réagi → mettre à jour
        Optional<Reaction> existing = reactionRepository
                .findByEventIdAndAuthorEmail(dto.getEventId(), dto.getAuthorEmail());

        Reaction reaction;
        if (existing.isPresent()) {
            reaction = existing.get();
            reaction.setType(dto.getType());
        } else {
            reaction = new Reaction();
            reaction.setAuthorName(dto.getAuthorName());
            reaction.setAuthorEmail(dto.getAuthorEmail());
            reaction.setEvent(event);
            reaction.setType(dto.getType());
        }

        return toDTO(reactionRepository.save(reaction));
    }

    @Override
    public void removeReaction(Long eventId, String authorEmail) {
        Reaction reaction = reactionRepository
                .findByEventIdAndAuthorEmail(eventId, authorEmail)
                .orElseThrow(() -> new BusinessException("Aucune réaction trouvée"));
        reactionRepository.delete(reaction);
    }

    @Override
    public ReactionSummaryDTO getReactionSummary(Long eventId) {
        ReactionSummaryDTO summary = new ReactionSummaryDTO();
        summary.setEventId(eventId);
        summary.setTotalReactions(reactionRepository.countByEventId(eventId));

        Map<ReactionType, Long> counts = new EnumMap<>(ReactionType.class);
        List<Object[]> results = reactionRepository.countByEventIdGroupByType(eventId);
        for (Object[] row : results) {
            counts.put((ReactionType) row[0], (Long) row[1]);
        }
        summary.setReactionCounts(counts);
        return summary;
    }
}