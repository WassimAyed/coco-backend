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

class ParticipantDTOTest {

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
        ParticipantDTO dto = ParticipantDTO.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .phone("12345678")
                .registrationDate(now)
                .eventId(10L)
                .userId(5L)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFullName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
        assertThat(dto.getPhone()).isEqualTo("12345678");
        assertThat(dto.getRegistrationDate()).isEqualTo(now);
        assertThat(dto.getEventId()).isEqualTo(10L);
        assertThat(dto.getUserId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Validation — should pass with valid fields")
    void validation_shouldPassWithValidFields() {
        ParticipantDTO dto = ParticipantDTO.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .phone("87654321")
                .eventId(20L)
                .build();

        Set<ConstraintViolation<ParticipantDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Validation — should fail when email is invalid")
    void validation_shouldFailWithInvalidEmail() {
        ParticipantDTO dto = ParticipantDTO.builder()
                .fullName("Jane Doe")
                .email("invalid-email")
                .phone("87654321")
                .eventId(20L)
                .build();

        Set<ConstraintViolation<ParticipantDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Format email invalide");
    }

    @Test
    @DisplayName("Validation — should fail when phone is invalid")
    void validation_shouldFailWithInvalidPhone() {
        ParticipantDTO dto = ParticipantDTO.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .phone("12345") // Only 5 digits
                .eventId(20L)
                .build();

        Set<ConstraintViolation<ParticipantDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Numéro de téléphone invalide (8 chiffres)");
    }
}
