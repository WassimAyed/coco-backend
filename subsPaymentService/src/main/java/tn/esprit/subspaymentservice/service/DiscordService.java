package tn.esprit.subspaymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordService {

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendNotification(String message) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("content", message);
            restTemplate.postForEntity(webhookUrl, body, String.class);
            log.info("Discord notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send Discord notification: {}", e.getMessage());
        }
    }

    public void sendSuccessPayment(String planName, Double amount, Long userId) {
        String msg = String.format("🚀 **Nouvelle Vente CoCo !**\n" +
                "💰 Plan : `%s`\n" +
                "💵 Montant : `%s TND`\n" +
                "👤 Utilisateur ID : `#%s`\n" +
                "✨ *Glory to ESPRIT!*", planName, amount, userId);
        sendNotification(msg);
    }
}
