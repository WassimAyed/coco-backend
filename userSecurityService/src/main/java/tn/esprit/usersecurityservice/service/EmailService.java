package tn.esprit.usersecurityservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import tn.esprit.usersecurityservice.validation.Validators;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.email-verification.expiration-minutes}")
    private long emailVerificationExpirationMinutes;

    @Value("${app.email-verification.url:http://localhost:8090/index.html}")
    private String emailVerificationUrl;

    @Value("classpath:templates/email-verification.html")
    private Resource emailVerificationTemplate;

    @Value("${app.two-factor.expiration-minutes}")
    private long twoFactorExpirationMinutes;

    @Value("${app.two-factor.verification-url:http://localhost:8090/index.html}")
    private String twoFactorVerificationUrl;

    @Value("classpath:templates/two-factor-login.html")
    private Resource twoFactorTemplate;

    public void sendEmailVerificationLink(String toEmail, String username, String verificationToken) {
        Validators.requireNonBlank(toEmail, "toEmail");
        Validators.requireNonBlank(username, "username");
        Validators.requireNonBlank(verificationToken, "verificationToken");
        try {
            String verificationLink = UriComponentsBuilder.fromUriString(emailVerificationUrl)
                    .queryParam("email", toEmail)
                    .queryParam("token", verificationToken)
                    .encode()
                    .toUriString();

            sendHtmlEmail(
                    toEmail,
                    "Verify your email address",
                    buildEmailVerificationTemplate(username, emailVerificationExpirationMinutes, verificationLink)
            );
        } catch (IOException ex) {
            throw new RuntimeException("Failed to prepare email verification message", ex);
        }
    }

    public void sendTwoFactorOtp(String toEmail, String username, String otpCode) {
        Validators.requireNonBlank(toEmail, "toEmail");
        Validators.requireNonBlank(username, "username");
        Validators.requireNonBlank(otpCode, "otpCode");
        try {
            sendHtmlEmail(
                    toEmail,
                    "Your login verification code",
                    buildTwoFactorTemplate(username, otpCode, twoFactorExpirationMinutes, twoFactorVerificationUrl)
            );
        } catch (IOException ex) {
            throw new RuntimeException("Failed to prepare two-factor email", ex);
        }
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send email", ex);
        }
    }

    private String buildEmailVerificationTemplate(
            String username,
            long expirationInMinutes,
            String actionUrl
    ) throws IOException {
        String displayName = (username == null || username.isBlank()) ? "there" : username;
        String template = new String(emailVerificationTemplate.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        return template
                .replace("{{username}}", displayName)
                .replace("{{expirationMinutes}}", String.valueOf(expirationInMinutes))
                .replace("{{verificationUrl}}", actionUrl);
    }

    private String buildTwoFactorTemplate(
            String username,
            String otpCode,
            long expirationInMinutes,
            String actionUrl
    ) throws IOException {
        String displayName = (username == null || username.isBlank()) ? "there" : username;
        String template = new String(twoFactorTemplate.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        return template
                .replace("{{username}}", displayName)
                .replace("{{otpCode}}", otpCode)
                .replace("{{expirationMinutes}}", String.valueOf(expirationInMinutes))
                .replace("{{verificationUrl}}", actionUrl);
    }
}
