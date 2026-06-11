package tn.esprit.collocationservice.Service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Unit Tests")
class EmailServiceTest {

    private static final String FROM_EMAIL = "sender@example.com";

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, FROM_EMAIL);
    }

    // =========================================================================
    // SEND OFFER EXPIRED EMAIL
    // =========================================================================
    @Nested
    @DisplayName("sendOfferExpiredEmail()")
    class SendOfferExpiredEmailTests {

        @Test
        @DisplayName("should send email with correct recipient, subject and body")
        void sendOfferExpiredEmail_shouldSendCorrectEmail() {
            // Arrange
            String toEmail = "owner@example.com";
            String offerTitle = "Studio Tunis Centre";

            // Act
            emailService.sendOfferExpiredEmail(toEmail, offerTitle);

            // Assert — capture the message sent
            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender, times(1)).send(captor.capture());

            SimpleMailMessage sent = captor.getValue();
            assertThat(sent.getTo()).contains(toEmail);
            assertThat(sent.getSubject()).isEqualTo("Your Collocation Offer has Expired");
            assertThat(sent.getFrom()).isEqualTo(FROM_EMAIL);
            assertThat(sent.getText()).contains(offerTitle);
            assertThat(sent.getText()).contains("expired");
        }

        @Test
        @DisplayName("should include offer title in email body")
        void sendOfferExpiredEmail_shouldIncludeOfferTitleInBody() {
            String title = "Villa Hammamet Premium";
            emailService.sendOfferExpiredEmail("user@test.com", title);

            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());

            assertThat(captor.getValue().getText()).contains(title);
        }

        @Test
        @DisplayName("should call mailSender.send exactly once per invocation")
        void sendOfferExpiredEmail_shouldCallMailSenderOnce() {
            emailService.sendOfferExpiredEmail("a@b.com", "TestOffer");
            verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("should set correct from address")
        void sendOfferExpiredEmail_shouldSetFromAddress() {
            emailService.sendOfferExpiredEmail("recipient@test.com", "Offer");

            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getFrom()).isEqualTo(FROM_EMAIL);
        }

        @Test
        @DisplayName("should propagate exception when mailSender fails")
        void sendOfferExpiredEmail_whenMailSenderFails_shouldPropagate() {
            doThrow(new RuntimeException("SMTP connection failed"))
                    .when(mailSender).send(any(SimpleMailMessage.class));

            assertThatThrownBy(() -> emailService.sendOfferExpiredEmail("fail@test.com", "BadOffer"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("SMTP connection failed");
        }
    }
}
