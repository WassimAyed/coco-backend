package tn.esprit.eventservice.service;

import tn.esprit.eventservice.dto.ReactionDTO;
import tn.esprit.eventservice.dto.ReactionSummaryDTO;

public interface IReactionService {
    ReactionDTO addOrUpdateReaction(ReactionDTO dto);
    void removeReaction(Long eventId, String authorEmail);
    ReactionSummaryDTO getReactionSummary(Long eventId);
}