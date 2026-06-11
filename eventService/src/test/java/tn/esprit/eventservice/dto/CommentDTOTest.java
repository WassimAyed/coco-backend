package tn.esprit.eventservice.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CommentDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Builder — should build DTO with all fields")
    void builder_shouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        CommentDTO dto = CommentDTO.builder()
                .id(1L)
                .content("Great event")
                .authorName("John")
                .authorEmail("john@example.com")
                .createdAt(now)
                .updatedAt(now)
                .eventId(10L)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getContent()).isEqualTo("Great event");
        assertThat(dto.getAuthorName()).isEqualTo("John");
        assertThat(dto.getAuthorEmail()).isEqualTo("john@example.com");
        assertThat(dto.getEventId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Validation — should pass with valid fields")
    void validation_shouldPassWithValidFields() {
        CommentDTO dto = CommentDTO.builder()
                .content("Valid comment")
                .authorName("Jane")
                .authorEmail("jane@example.com")
                .eventId(5L)
                .build();

        Set<ConstraintViolation<CommentDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Validation — should fail when fields are missing")
    void validation_shouldFailWhenFieldsAreMissing() {
        CommentDTO dto = new CommentDTO();

        Set<ConstraintViolation<CommentDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(4); // content, authorName, authorEmail, eventId
    }

    @Test
    @DisplayName("Validation — should fail when content exceeds max length")
    void validation_shouldFailWhenContentTooLong() {
        String longContent = "a".repeat(1001);
        CommentDTO dto = CommentDTO.builder()
                .content(longContent)
                .authorName("Jane")
                .authorEmail("jane@example.com")
                .eventId(5L)
                .build();

        Set<ConstraintViolation<CommentDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Le commentaire ne doit pas dépasser 1000 caractères");
    }
}
