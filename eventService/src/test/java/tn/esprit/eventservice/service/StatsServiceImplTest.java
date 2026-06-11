package tn.esprit.eventservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventservice.dto.StatsDTO;
import tn.esprit.eventservice.repository.CategoryRepository;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.repository.ParticipantRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    @Test
    @DisplayName("getGlobalStats — should aggregate stats from all repositories")
    void getGlobalStats_shouldAggregateStats() {
        // Given
        given(eventRepository.count()).willReturn(10L);
        given(participantRepository.count()).willReturn(50L);
        given(categoryRepository.count()).willReturn(5L);
        given(eventRepository.countAvailableEvents()).willReturn(7L);

        List<Object[]> statusRow = java.util.Collections.singletonList(new Object[]{"PENDING", 4L});
        given(eventRepository.countByStatus()).willReturn(statusRow);

        List<Object[]> categoryRow = java.util.Collections.singletonList(new Object[]{"Music", 6L});
        given(eventRepository.countByCategory()).willReturn(categoryRow);

        List<Object[]> eventRow = java.util.Collections.singletonList(new Object[]{"Festival", 20L});
        given(participantRepository.countByEvent()).willReturn(eventRow);

        // When
        StatsDTO result = statsService.getGlobalStats();

        // Then
        assertThat(result.getTotalEvents()).isEqualTo(10L);
        assertThat(result.getTotalParticipants()).isEqualTo(50L);
        assertThat(result.getTotalCategories()).isEqualTo(5L);
        assertThat(result.getAvailableEvents()).isEqualTo(7L);
        
        assertThat(result.getEventsByStatus()).containsEntry("PENDING", 4L);
        assertThat(result.getEventsByCategory()).containsEntry("Music", 6L);
        assertThat(result.getParticipantsByEvent()).containsEntry("Festival", 20L);
    }
}
