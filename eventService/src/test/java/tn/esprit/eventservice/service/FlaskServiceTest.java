package tn.esprit.eventservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import tn.esprit.eventservice.config.FlaskApiConfig;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FlaskServiceTest {

    @Mock
    private FlaskApiConfig flaskApiConfig;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FlaskService flaskService;

    @Test
    @DisplayName("callFlask_shouldReturnResponseBody_whenFlaskResponds")
    void callFlask_shouldReturnResponseBody_whenFlaskResponds() {
        given(flaskApiConfig.getUrl()).willReturn("http://flask-service");
        given(flaskApiConfig.getToken()).willReturn("secret-token");

        ResponseEntity<String> fakeResponse = new ResponseEntity<>(
                "{\"prediction\": 120}", HttpStatus.OK);

        given(restTemplate.exchange(
                eq("http://flask-service/predict"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).willReturn(fakeResponse);

        String result = flaskService.callFlask(Map.of("eventId", 1L));

        assertThat(result).isEqualTo("{\"prediction\": 120}");
    }

    @Test
    @DisplayName("callFlask_shouldSendBearerToken_inAuthorizationHeader")
    void callFlask_shouldSendBearerToken_inAuthorizationHeader() {
        given(flaskApiConfig.getUrl()).willReturn("http://flask-service");
        given(flaskApiConfig.getToken()).willReturn("my-secret");

        given(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).willReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        String result = flaskService.callFlask(Map.of("data", "value"));

        assertThat(result).isEqualTo("ok");
    }

    @Test
    @DisplayName("callFlask_shouldThrowException_whenRestTemplateThrows")
    void callFlask_shouldThrowException_whenRestTemplateThrows() {
        given(flaskApiConfig.getUrl()).willReturn("http://flask-service");
        given(flaskApiConfig.getToken()).willReturn("token");

        given(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).willThrow(new RuntimeException("Connection refused"));

        Map<String, Object> requestData = Map.of();
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> flaskService.callFlask(requestData));

        assertThat(ex.getMessage()).contains("Connection refused");
    }

    @Test
    @DisplayName("callFlask_shouldReturnNull_whenResponseBodyIsNull")
    void callFlask_shouldReturnNull_whenResponseBodyIsNull() {
        given(flaskApiConfig.getUrl()).willReturn("http://flask-service");
        given(flaskApiConfig.getToken()).willReturn("token");

        given(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).willReturn(new ResponseEntity<>((String) null, HttpStatus.OK));

        String result = flaskService.callFlask(Map.of());

        assertThat(result).isNull();
    }
}