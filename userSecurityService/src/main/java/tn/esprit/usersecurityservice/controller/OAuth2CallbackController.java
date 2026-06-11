package tn.esprit.usersecurityservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OAuth2CallbackController {
    @GetMapping("/oauth2/callback")
    public Map<String, String> callback(
            @RequestParam String accessToken,
            @RequestParam String refreshToken
    ) {
        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    @GetMapping("/oauth2/error")
    public Map<String, String> error(@RequestParam String message) {
        return Map.of("error", message);
    }
}
