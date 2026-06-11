package tn.esprit.eventservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventservice.dto.ReactionDTO;
import tn.esprit.eventservice.dto.ReactionSummaryDTO;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.Reaction;
import tn.esprit.eventservice.entity.ReactionType;
import tn.esprit.eventservice.exception.BusinessException;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.repository.ReactionRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReactionServiceImplTest {

    @Mock
    private ReactionRepository reactionRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ReactionServiceImpl reactionService;

    @Test
    @DisplayName("addOrUpdateReaction — should create new reaction when none exists")
    void addOrUpdateReaction_shouldCreateNew_whenNoneExists() {
        // Given
        ReactionDTO dto = buildReactionDto();
        Event event = new Event();
        event.setId(1L);

        Reaction saved = new Reaction();
        saved.setId(10L);
        saved.setEvent(event);
        saved.setType(ReactionType.LIKE);
        saved.setAuthorEmail("user@example.com");

        given(eventRepository.findById(1L)).willReturn(Optional.of(event));
        given(reactionRepository.findByEventIdAndAuthorEmail(1L, "user@example.com")).willReturn(Optional.empty());
        given(reactionRepository.save(any(Reaction.class))).willReturn(saved);

        // When
        ReactionDTO result = reactionService.addOrUpdateReaction(dto);

        // Then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getType()).isEqualTo(ReactionType.LIKE);
        verify(reactionRepository, times(1)).save(any(Reaction.class));
    }

    @Test
    @DisplayName("addOrUpdateReaction — should update existing reaction")
    void addOrUpdateReaction_shouldUpdateExisting() {
        // Given
        ReactionDTO dto = buildReactionDto();
        dto.setType(ReactionType.LOVE);
        
        Event event = new Event();
        event.setId(1L);

        Reaction existing = new Reaction();
        existing.setId(10L);
        existing.setEvent(event);
        existing.setType(ReactionType.LIKE);
        existing.setAuthorEmail("user@example.com");

        given(eventRepository.findById(1L)).willReturn(Optional.of(event));
        given(reactionRepository.findByEventIdAndAuthorEmail(1L, "user@example.com")).willReturn(Optional.of(existing));
        given(reactionRepository.save(any(Reaction.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        ReactionDTO result = reactionService.addOrUpdateReaction(dto);

        // Then
        assertThat(result.getType()).isEqualTo(ReactionType.LOVE);
        verify(reactionRepository, times(1)).save(any(Reaction.class));
    }

    @Test
    @DisplayName("addOrUpdateReaction — should throw exception when event not found")
    void addOrUpdateReaction_shouldThrowException_whenEventNotFound() {
        // Given
        ReactionDTO dto = buildReactionDto();
        given(eventRepository.findById(1L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reactionService.addOrUpdateReaction(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("removeReaction — should delete when exists")
    void removeReaction_shouldDelete_whenExists() {
        // Given
        Reaction reaction = new Reaction();
        given(reactionRepository.findByEventIdAndAuthorEmail(1L, "user@example.com")).willReturn(Optional.of(reaction));

        // When
        reactionService.removeReaction(1L, "user@example.com");

        // Then
        verify(reactionRepository, times(1)).delete(reaction);
    }

    @Test
    @DisplayName("removeReaction — should throw exception when not found")
    void removeReaction_shouldThrowException_whenNotFound() {
        // Given
        given(reactionRepository.findByEventIdAndAuthorEmail(1L, "user@example.com")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reactionService.removeReaction(1L, "user@example.com"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("getReactionSummary — should return counts grouped by type")
    void getReactionSummary_shouldReturnCounts() {
        // Given
        given(reactionRepository.countByEventId(1L)).willReturn(15L);
        Object[] row1 = new Object[]{ReactionType.LIKE, 10L};
        Object[] row2 = new Object[]{ReactionType.LOVE, 5L};
        given(reactionRepository.countByEventIdGroupByType(1L)).willReturn(List.of(row1, row2));

        // When
        ReactionSummaryDTO summary = reactionService.getReactionSummary(1L);

        // Then
        assertThat(summary.getTotalReactions()).isEqualTo(15L);
        assertThat(summary.getReactionCounts()).containsEntry(ReactionType.LIKE, 10L);
        assertThat(summary.getReactionCounts()).containsEntry(ReactionType.LOVE, 5L);
    }

    private ReactionDTO buildReactionDto() {
        ReactionDTO dto = new ReactionDTO();
        dto.setEventId(1L);
        dto.setType(ReactionType.LIKE);
        dto.setAuthorEmail("user@example.com");
        dto.setAuthorName("User");
        return dto;
    }
}
