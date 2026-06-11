package tn.esprit.collocationservice.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendOfferExpiredEmail(String toEmail, String offerTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your Collocation Offer has Expired");
        message.setText("Dear user,\n\nYour collocation offer titled '" + offerTitle + "' has expired.\n\nThank you,\nCollocation Service Team");

        mailSender.send(message);
    }
}
