package tn.esprit.usersecurityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private boolean requiresTwoFactor;
    private String message;
    private String accessToken;
    private String refreshToken;
    private String tokenType;

    public static LoginResponse tokens(String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .requiresTwoFactor(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    public static LoginResponse twoFactorRequired(String message) {
        return LoginResponse.builder()
                .requiresTwoFactor(true)
                .message(message)
                .build();
    }
}
