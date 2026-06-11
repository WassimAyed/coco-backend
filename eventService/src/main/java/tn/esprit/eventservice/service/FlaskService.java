package tn.esprit.eventservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.eventservice.config.FlaskApiConfig;

@Service
public class FlaskService {

    private static final Logger log = LoggerFactory.getLogger(FlaskService.class);

    private final FlaskApiConfig flaskApiConfig;
    private final RestTemplate restTemplate;

    public FlaskService(FlaskApiConfig flaskApiConfig, RestTemplate restTemplate) {
        this.flaskApiConfig = flaskApiConfig;
        this.restTemplate = restTemplate;
    }

    public String callFlask(Object requestBody) {
        log.info("Calling Flask prediction endpoint at {}", flaskApiConfig.getUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + flaskApiConfig.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                flaskApiConfig.getUrl() + "/predict",
                HttpMethod.POST,
                entity,
                String.class
        );

        return response.getBody();
    }

}