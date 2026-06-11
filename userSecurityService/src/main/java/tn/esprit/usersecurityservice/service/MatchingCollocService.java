package tn.esprit.usersecurityservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.usersecurityservice.dto.MatchCollocRespDTO;
import tn.esprit.usersecurityservice.dto.MatchRequestDTOColl;
import tn.esprit.usersecurityservice.dto.ProfileRequestDTO;
import tn.esprit.usersecurityservice.utils.MatchingMapper;
import tn.esprit.usersecurityservice.validation.Validators;

import java.util.List;

@Service
public class MatchingCollocService {

    @Autowired
    private RestTemplate restTemplate;

    private final String ML_URL = "http://127.0.0.1:5034/match ";

    public MatchCollocRespDTO[] callMatching(
            ProfileRequestDTO user,
            List<ProfileRequestDTO> candidates) {
        Validators.requireNonNull(user, "user");
        Validators.requireNonNull(candidates, "candidates");

        // ✅ Convert Spring DTO → ML payload
        MatchRequestDTOColl request =
                MatchingMapper.toMatchRequest(user, candidates);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MatchRequestDTOColl> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<MatchCollocRespDTO[]> response =
                restTemplate.exchange(
                        ML_URL,
                        HttpMethod.POST,
                        entity,
                        MatchCollocRespDTO[].class
                );

        return response.getBody();
    }
}