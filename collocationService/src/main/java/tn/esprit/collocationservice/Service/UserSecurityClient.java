package tn.esprit.collocationservice.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSecurityClient {

    private final RestTemplate restTemplate;

    public UserDTO findUserById(Long ownerId) {
        try {
            String url = "http://localhost:8090/users/" + ownerId;
            return restTemplate.getForObject(url, UserDTO.class);
        } catch (Exception e) {
            log.error("Failed to fetch user with id {}: {}", ownerId, e.getMessage());
            return null;
        }
    }
}