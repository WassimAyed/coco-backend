package tn.esprit.serviceetudiant.websocket;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import tn.esprit.serviceetudiant.dto.ChatMessageRequest;
import tn.esprit.serviceetudiant.dto.ChatTypingRequest;
import tn.esprit.serviceetudiant.service.ChatService;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;

    @MessageMapping("/student-services/chat.send")
    public void sendMessage(@Valid ChatMessageRequest request) {
        chatService.sendMessage(request);
    }

    @MessageMapping("/student-services/chat.typing")
    public void publishTyping(@Valid ChatTypingRequest request) {
        chatService.publishTypingIndicator(request);
    }
}
