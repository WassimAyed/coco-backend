package tn.esprit.subspaymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("mohiyed30@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.warn("Could not send email to {}: {}. Ensure SMTP is configured.", to, e.getMessage());
        }
    }

    public void sendExpirationWarning(String to, String planName, int daysRemaining) {
        String subject = "⚠️ Votre abonnement CoCo expire bientôt !";
        String body = String.format("Bonjour,\n\n" +
                "Votre abonnement au plan %s expire dans %d jours.\n" +
                "Renouvelez dès maintenant pour ne pas perdre vos avantages !\n\n" +
                "L'équipe CoCo\n" +
                "Glory to ESPRIT!", planName, daysRemaining);
        sendEmail(to, subject, body);
    }
}
