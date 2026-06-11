package tn.esprit.eventservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    // ─────────────────────────────────────────────
    // ResourceNotFoundException → 404
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("handleNotFound_shouldReturn404_withCorrectBody")
    void handleNotFound_shouldReturn404_withCorrectBody() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException("Événement introuvable : 99");

        // When
        ResponseEntity<ErrorResponse> result = handler.handleNotFound(ex);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getStatus()).isEqualTo(404);
        assertThat(result.getBody().getError()).isEqualTo("NOT_FOUND");
        assertThat(result.getBody().getMessage()).isEqualTo("Événement introuvable : 99");
        assertThat(result.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleNotFound_shouldReturn404_whenCategoryNotFound")
    void handleNotFound_shouldReturn404_whenCategoryNotFound() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException("Catégorie introuvable : 5");

        // When
        ResponseEntity<ErrorResponse> result = handler.handleNotFound(ex);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody().getMessage()).contains("Catégorie introuvable");
    }

    // ─────────────────────────────────────────────
    // BusinessException → 400
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("handleBusiness_shouldReturn400_withCorrectBody")
    void handleBusiness_shouldReturn400_withCorrectBody() {
        // Given
        BusinessException ex = new BusinessException("La date de fin doit être après la date de début");

        // When
        ResponseEntity<ErrorResponse> result = handler.handleBusiness(ex);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getStatus()).isEqualTo(400);
        assertThat(result.getBody().getError()).isEqualTo("BUSINESS_ERROR");
        assertThat(result.getBody().getMessage()).isEqualTo("La date de fin doit être après la date de début");
    }

    // ─────────────────────────────────────────────
    // MethodArgumentNotValidException → 400 VALIDATION
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("handleValidation_shouldReturn400_withFieldErrors")
    void handleValidation_shouldReturn400_withFieldErrors() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("eventRatingDTO", "rating", "must be between 1 and 5");

        given(ex.getBindingResult()).willReturn(bindingResult);
        given(bindingResult.getFieldErrors()).willReturn(List.of(fieldError));

        // When
        ResponseEntity<ErrorResponse> result = handler.handleValidation(ex);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo("VALIDATION_ERROR");
        assertThat(result.getBody().getMessage()).contains("rating");
        assertThat(result.getBody().getMessage()).contains("must be between 1 and 5");
    }

    // ─────────────────────────────────────────────
    // MethodArgumentTypeMismatchException → 400
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("handleTypeMismatch_shouldReturn400_withParamName")
    void handleTypeMismatch_shouldReturn400_withParamName() {
        // Given
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        given(ex.getName()).willReturn("eventId");

        // When
        ResponseEntity<ErrorResponse> result = handler.handleTypeMismatch(ex);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo("BAD_REQUEST");
        assertThat(result.getBody().getMessage()).contains("eventId");
    }

    // ─────────────────────────────────────────────
    // Generic Exception → 500
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("handleGeneric_shouldReturn500_forUnexpectedException")
    void handleGeneric_shouldReturn500_forUnexpectedException() {
        // Given
        Exception ex = new RuntimeException("Erreur inattendue");

        // When
        ResponseEntity<ErrorResponse> result = handler.handleGeneric(ex);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getStatus()).isEqualTo(500);
        assertThat(result.getBody().getError()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(result.getBody().getMessage()).isEqualTo("Erreur inattendue");
    }

    @Test
    @DisplayName("handleGeneric_shouldReturn500_withTimestamp")
    void handleGeneric_shouldReturn500_withTimestamp() {
        // Given
        Exception ex = new NullPointerException("NPE simulé");

        // When
        ResponseEntity<ErrorResponse> result = handler.handleGeneric(ex);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody().getTimestamp()).isNotNull();
    }

    // ─────────────────────────────────────────────
    // Exception classes themselves
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("ResourceNotFoundException_shouldStoreMessage")
    void resourceNotFoundException_shouldStoreMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        assertThat(ex.getMessage()).isEqualTo("Not found");
    }

    @Test
    @DisplayName("BusinessException_shouldStoreMessage")
    void businessException_shouldStoreMessage() {
        BusinessException ex = new BusinessException("Business rule violated");
        assertThat(ex.getMessage()).isEqualTo("Business rule violated");
    }
}