package tn.esprit.collocationservice.Exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("should map not found exceptions to 404")
    void notFoundHandlers_shouldReturn404() {
        ResponseEntity<Object> offerResponse = handler.handleOfferNotFound(new OfferNotFoundException("offer missing"));
        ResponseEntity<Object> requestResponse = handler.handleRequestNotFound(new RequestNotFoundException("request missing"));

        assertErrorBody(offerResponse, HttpStatus.NOT_FOUND, "offer missing");
        assertErrorBody(requestResponse, HttpStatus.NOT_FOUND, "request missing");
    }

    @Test
    @DisplayName("should map image storage failures to 500 with generic message")
    void handleImageStorage_shouldReturn500() {
        ResponseEntity<Object> response = handler.handleImageStorage(
                new ImageStorageException("disk failed", new RuntimeException("io")));

        assertErrorBody(response, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image");
    }

    @Test
    @DisplayName("should map illegal arguments to 400")
    void handleIllegalArgument_shouldReturn400() {
        ResponseEntity<Object> response = handler.handleIllegalArgument(new IllegalArgumentException("bad input"));

        assertErrorBody(response, HttpStatus.BAD_REQUEST, "bad input");
    }

    @Test
    @DisplayName("should map unexpected exceptions to 500 with generic message")
    void handleGeneralException_shouldReturn500() {
        ResponseEntity<Object> response = handler.handleGeneralException(new RuntimeException("boom"));

        assertErrorBody(response, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private void assertErrorBody(ResponseEntity<Object> response, HttpStatus status, String message) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertThat(body)
                .containsEntry("status", status.value())
                .containsEntry("message", message);
        assertThat(body).containsKey("timestamp");
    }
}
