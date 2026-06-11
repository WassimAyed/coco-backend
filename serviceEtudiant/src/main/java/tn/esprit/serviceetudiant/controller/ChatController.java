package tn.esprit.serviceetudiant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.serviceetudiant.dto.ChatMessageRequest;
import tn.esprit.serviceetudiant.dto.ChatMessageResponse;
import tn.esprit.serviceetudiant.dto.ChatMessageUpdateRequest;
import tn.esprit.serviceetudiant.dto.ConversationResponse;
import tn.esprit.serviceetudiant.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/student-services/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @RequestParam Long participantId
    ) {
        return ResponseEntity.ok(chatService.getConversationsForParticipant(participantId));
    }

    @GetMapping("/conversations/request/{requestId}")
    public ResponseEntity<ConversationResponse> getConversationByRequest(
            @PathVariable Long requestId,
            @RequestParam Long participantId
    ) {
        return ResponseEntity.ok(chatService.getConversationByRequest(requestId, participantId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam Long participantId
    ) {
        return ResponseEntity.ok(chatService.getMessages(conversationId, participantId));
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @Valid @RequestBody ChatMessageRequest request
    ) {
        return ResponseEntity.ok(chatService.sendMessage(request));
    }

    @PutMapping("/messages/{messageId}")
    public ResponseEntity<ChatMessageResponse> updateMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody ChatMessageUpdateRequest request
    ) {
        return ResponseEntity.ok(chatService.updateMessage(messageId, request));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<ChatMessageResponse> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long participantId
    ) {
        return ResponseEntity.ok(chatService.deleteMessage(messageId, participantId));
    }

    @PostMapping(value = "/messages/image", consumes = "multipart/form-data")
    public ResponseEntity<ChatMessageResponse> sendImageMessage(
            @RequestParam Long conversationId,
            @RequestParam Long senderId,
            @RequestParam String senderName,
            @RequestParam(required = false) String content,
            @RequestPart("image") MultipartFile image
    ) {
        return ResponseEntity.ok(chatService.sendImageMessage(conversationId, senderId, senderName, content, image));
    }
}
