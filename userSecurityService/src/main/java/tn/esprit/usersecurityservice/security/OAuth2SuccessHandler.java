package tn.esprit.usersecurityservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tn.esprit.usersecurityservice.Enum.Role;
import tn.esprit.usersecurityservice.entity.RefreshToken;
import tn.esprit.usersecurityservice.entity.User;
import tn.esprit.usersecurityservice.repository.UserRepository;
import tn.esprit.usersecurityservice.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    private final String FRONTEND_CALLBACK = "http://localhost:4200/oauth2/callback";

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, 
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException{
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name  = oAuth2User.getAttribute("name");
        String pictureUrl  = oAuth2User.getAttribute("picture");

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Email not provided by OAuth2 provider");
        }
        // (1) Find or create local user
        String lastname = name != null ? name : "GoogleUser";
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = User.builder()
                    .email(email)
                    .username(name != null ? name : email)
                    .lastname(lastname)
                    .role(Role.USER)
                    .imageUrl(pictureUrl)
                    // Social account: you can store a dummy value or nullable password (better: allow null)
                    .password("GOOGLE_AUTH")
                    .build();
            return userRepository.save(u);
        });

        if (pictureUrl != null && (user.getImageUrl() == null || !pictureUrl.equals(user.getImageUrl()))) {
            user.setImageUrl(pictureUrl);
            userRepository.save(user);
        }

        if (!user.isEnabled()) {
            getRedirectStrategy().sendRedirect(request, response, "http://localhost:4200/account-disabled");
            return;
        }
        if (user.isLocked()) {
            getRedirectStrategy().sendRedirect(request, response, "http://localhost:4200/account-disabled");
            return;
        }

        // (2) Generate YOUR tokens
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createOrUpdateRefreshToken(user.getId());

        // (3) Redirect back to frontend with tokens
        String redirectUrl = FRONTEND_CALLBACK
                + "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken.getToken(), StandardCharsets.UTF_8);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
