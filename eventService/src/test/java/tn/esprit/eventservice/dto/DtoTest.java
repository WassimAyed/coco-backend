package tn.esprit.eventservice.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTest {

    // ─────────────────────────────────────────────
    // BehaviorDTO
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("BehaviorDTO_builder_shouldSetAllFields")
    void behaviorDTO_builder_shouldSetAllFields() {
        BehaviorDTO dto = BehaviorDTO.builder()
                .userId(1L)
                .eventId(10L)
                .categoryId(2L)
                .actionType("VIEW")
                .lat(36.8)
                .lng(10.1)
                .build();

        assertThat(dto)
                .extracting(BehaviorDTO::getUserId, BehaviorDTO::getEventId,
                        BehaviorDTO::getCategoryId, BehaviorDTO::getActionType,
                        BehaviorDTO::getLat, BehaviorDTO::getLng)
                .containsExactly(1L, 10L, 2L, "VIEW", 36.8, 10.1);
    }

    @Test
    @DisplayName("BehaviorDTO_setters_shouldUpdateFields")
    void behaviorDTO_setters_shouldUpdateFields() {
        BehaviorDTO dto = new BehaviorDTO();
        dto.setUserId(5L);
        dto.setActionType("PARTICIPATE");

        assertThat(dto.getUserId()).isEqualTo(5L);
        assertThat(dto.getActionType()).isEqualTo("PARTICIPATE");
    }

    // ─────────────────────────────────────────────
    // EventRatingDTO
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("EventRatingDTO_builder_shouldSetAllFields")
    void eventRatingDTO_builder_shouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        EventRatingDTO dto = EventRatingDTO.builder()
                .id(1L)
                .eventId(10L)
                .userId(2L)
                .rating(5)
                .createdAt(now)
                .averageRating(4.5)
                .totalRatings(20L)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getRating()).isEqualTo(5);
        assertThat(dto.getAverageRating()).isEqualTo(4.5);
        assertThat(dto.getTotalRatings()).isEqualTo(20L);
        assertThat(dto.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("EventRatingDTO_setters_shouldUpdateFields")
    void eventRatingDTO_setters_shouldUpdateFields() {
        EventRatingDTO dto = new EventRatingDTO();
        dto.setRating(3);
        dto.setAverageRating(3.0);
        dto.setTotalRatings(5L);

        assertThat(dto.getRating()).isEqualTo(3);
        assertThat(dto.getAverageRating()).isEqualTo(3.0);
    }

    // ─────────────────────────────────────────────
    // PagedResponse
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("PagedResponse_builder_shouldSetAllFields")
    void pagedResponse_builder_shouldSetAllFields() {
        PagedResponse<String> response = PagedResponse.<String>builder()
                .content(List.of("a", "b"))
                .page(0)
                .size(9)
                .totalElements(2L)
                .totalPages(1)
                .last(true)
                .build();

        assertThat(response.getContent()).containsExactly("a", "b");
        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(9);
        assertThat(response.getTotalElements()).isEqualTo(2L);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();
    }

    // ─────────────────────────────────────────────
    // ScoredEventDTO
    // ─────────────────────────────────────────────

    @ParameterizedTest(name = "score={0} => label={1}")
    @MethodSource("scoreLabelProvider")
    @DisplayName("ScoredEventDTO_scoreLabel_shouldReturnCorrectLabel")
    void scoredEventDTO_scoreLabel_shouldReturnCorrectLabel(double score, String expectedLabel) {
        ScoredEventDTO scored = new ScoredEventDTO(new EventDTO(), score);
        assertThat(scored.getScoreLabel()).isEqualTo(expectedLabel);
    }

    static Stream<Arguments> scoreLabelProvider() {
        return Stream.of(
                Arguments.of(0.9, "Top pick"),
                Arguments.of(0.7, "Recommande"),
                Arguments.of(0.4, "Peut vous interesser")
        );}

    @Test
    @DisplayName("ScoredEventDTO_builder_shouldSetAllFields")
    void scoredEventDTO_builder_shouldSetAllFields() {
        EventDTO event = new EventDTO();
        event.setId(1L);

        ScoredEventDTO scored = ScoredEventDTO.builder()
                .event(event)
                .score(0.85)
                .scoreLabel("Top pick")
                .build();

        assertThat(scored.getEvent().getId()).isEqualTo(1L);
        assertThat(scored.getScore()).isEqualTo(0.85);
        assertThat(scored.getScoreLabel()).isEqualTo("Top pick");
    }

    // ─────────────────────────────────────────────
    // SimilarEventDTO
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("SimilarEventDTO_setters_shouldUpdateAllFields")
    void similarEventDTO_setters_shouldUpdateAllFields() {
        SimilarEventDTO dto = new SimilarEventDTO();
        dto.setId(1L);
        dto.setName("Festival Jazz");
        dto.setLocation("Tunis");
        dto.setImageUrl("http://img.jpg");
        dto.setMaxCapacity(200);
        dto.setCurrentParticipants(50);
        dto.setCategoryName("Music");
        dto.setAverageRating(4.2);
        dto.setSimilarityReason("Same category");

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Festival Jazz");
        assertThat(dto.getCategoryName()).isEqualTo("Music");
        assertThat(dto.getAverageRating()).isEqualTo(4.2);
        assertThat(dto.getSimilarityReason()).isEqualTo("Same category");
    }

    // ─────────────────────────────────────────────
    // PredictionRequest
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("PredictionRequest_builder_shouldSetAllFields")
    void predictionRequest_builder_shouldSetAllFields() {
        PredictionRequest req = PredictionRequest.builder()
                .categoryId(1L)
                .price(25.0)
                .isFree(0)
                .maxCapacity(100)
                .eventType("CONCERT")
                .isWeekend(1)
                .isHoliday(0)
                .durationDays(2)
                .daysUntilEvent(10)
                .temperature(22.0)
                .precipitationMm(0.0)
                .windSpeedKmh(15.0)
                .build();

        assertThat(req.getCategoryId()).isEqualTo(1L);
        assertThat(req.getPrice()).isEqualTo(25.0);
        assertThat(req.getIsFree()).isZero();
        assertThat(req.getMaxCapacity()).isEqualTo(100);
        assertThat(req.getEventType()).isEqualTo("CONCERT");
        assertThat(req.getIsWeekend()).isEqualTo(1);
        assertThat(req.getDurationDays()).isEqualTo(2);
        assertThat(req.getTemperature()).isEqualTo(22.0);
    }

    // ─────────────────────────────────────────────
    // PredictionResponse
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("PredictionResponse_builder_shouldSetPredictedParticipants")
    void predictionResponse_builder_shouldSetPredictedParticipants() {
        PredictionResponse resp = PredictionResponse.builder()
                .predictedParticipants(75)
                .build();

        assertThat(resp.getPredictedParticipants()).isEqualTo(75);
    }

    @Test
    @DisplayName("PredictionResponse_noArgsConstructor_shouldHaveNullField")
    void predictionResponse_noArgsConstructor_shouldHaveNullField() {
        PredictionResponse resp = new PredictionResponse();
        assertThat(resp.getPredictedParticipants()).isNull();
    }
}
