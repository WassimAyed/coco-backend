package tn.esprit.eventservice.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.esprit.eventservice.entity.ReactionType;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReactionDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Builder — should build DTO with all fields")
    void builder_shouldSetAllFields() {
        ReactionDTO dto = ReactionDTO.builder()
                .id(1L)
                .type(ReactionType.LIKE)
                .authorName("User Name")
                .authorEmail("user@example.com")
                .eventId(10L)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getType()).isEqualTo(ReactionType.LIKE);
        assertThat(dto.getAuthorName()).isEqualTo("User Name");
        assertThat(dto.getAuthorEmail()).isEqualTo("user@example.com");
        assertThat(dto.getEventId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Setters — should update fields correctly")
    void setters_shouldUpdateFields() {
        ReactionDTO dto = new ReactionDTO();
        dto.setId(2L);
        dto.setType(ReactionType.LOVE);
        dto.setAuthorName("Jane");
        dto.setAuthorEmail("jane@example.com");
        dto.setEventId(20L);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getType()).isEqualTo(ReactionType.LOVE);
        assertThat(dto.getAuthorName()).isEqualTo("Jane");
        assertThat(dto.getAuthorEmail()).isEqualTo("jane@example.com");
        assertThat(dto.getEventId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("Validation — should fail when required fields are missing")
    void validation_shouldFailWhenMissingFields() {
        ReactionDTO dto = new ReactionDTO();

        Set<ConstraintViolation<ReactionDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(4); // type, authorName, authorEmail, eventId
    }

    @Test
    @DisplayName("Validation — should pass when all valid fields are provided")
    void validation_shouldPassWhenValidFields() {
        ReactionDTO dto = ReactionDTO.builder()
                .type(ReactionType.LIKE)
                .authorName("John")
                .authorEmail("john@example.com")
                .eventId(5L)
                .build();

        Set<ConstraintViolation<ReactionDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
