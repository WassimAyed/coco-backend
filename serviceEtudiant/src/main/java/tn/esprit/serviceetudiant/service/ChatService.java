package tn.esprit.serviceetudiant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.serviceetudiant.dto.ChatMessageRequest;
import tn.esprit.serviceetudiant.dto.ChatMessageResponse;
import tn.esprit.serviceetudiant.dto.ChatMessageUpdateRequest;
import tn.esprit.serviceetudiant.dto.ChatTypingRequest;
import tn.esprit.serviceetudiant.dto.ChatTypingResponse;
import tn.esprit.serviceetudiant.dto.ConversationResponse;
import tn.esprit.serviceetudiant.dto.CoverUploadResponse;
import tn.esprit.serviceetudiant.entity.ChatConversation;
import tn.esprit.serviceetudiant.entity.ChatMessage;
import tn.esprit.serviceetudiant.entity.ServiceRequest;
import tn.esprit.serviceetudiant.enums.ServiceRequestStatus;
import tn.esprit.serviceetudiant.exception.ConflictException;
import tn.esprit.serviceetudiant.exception.NotFoundException;
import tn.esprit.serviceetudiant.mapper.ChatMessageMapper;
import tn.esprit.serviceetudiant.repository.ChatConversationRepository;
import tn.esprit.serviceetudiant.repository.ChatMessageRepository;
import tn.esprit.serviceetudiant.repository.ServiceRequestRepository;
import tn.esprit.serviceetudiant.validation.Validators;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatConversationRepository chatConversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final StorageGatewayService storageGatewayService;
    private final ChatMessageMapper chatMessageMapper;

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationsForParticipant(Long participantId) {
        Validators.requirePositive(participantId, "participantId");
        return chatConversationRepository.findByRequesterIdOrProviderIdOrderByUpdatedAtDesc(participantId, participantId)
                .stream()
                .map(conversation -> toConversationResponse(conversation, participantId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversationByRequest(Long requestId, Long participantId) {
        Validators.requirePositive(requestId, "requestId");
        Validators.requirePositive(participantId, "participantId");
        ChatConversation conversation = chatConversationRepository.findByRequestId(requestId)
                .orElseThrow(() -> new NotFoundException("Conversation not found for this request."));
        validateParticipant(conversation, participantId);
        return toConversationResponse(conversation, participantId);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long conversationId, Long participantId) {
        Validators.requirePositive(conversationId, "conversationId");
        Validators.requirePositive(participantId, "participantId");
        ChatConversation conversation = findConversation(conversationId);
        validateParticipant(conversation, participantId);
        return chatMessageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(chatMessageMapper::toResponse)
                .toList();
    }

    @Transactional
    public ConversationResponse ensureConversationForAcceptedRequest(ServiceRequest request) {
        Validators.requireNonNull(request, "request");
        if (request.getStatus() != ServiceRequestStatus.ACCEPTED && request.getStatus() != ServiceRequestStatus.COMPLETED) {
            throw new ConflictException("Chat is only available after the request is accepted.");
        }

        ChatConversation conversation = chatConversationRepository.findByRequestId(request.getId())
                .orElseGet(() -> chatConversationRepository.save(ChatConversation.builder()
                        .requestId(request.getId())
                        .serviceId(request.getServiceId())
                        .serviceTitle(request.getServiceTitle())
                        .requesterId(request.getRequesterId())
                        .requesterName(request.getRequesterName())
                        .providerId(request.getProviderId())
                        .providerName(request.getProviderName())
                        .active(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()));

        if (!conversation.isActive()) {
            conversation.setActive(true);
            conversation.setUpdatedAt(Instant.now());
            conversation = chatConversationRepository.save(conversation);
        }

        return toConversationResponse(conversation, request.getRequesterId());
    }

    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        Validators.requireNonNull(request, "request");
        Validators.requirePositive(request.conversationId(), "conversationId");
        Validators.requirePositive(request.senderId(), "senderId");
        ChatConversation conversation = findConversation(request.conversationId());
        validateParticipant(conversation, request.senderId());
        validateMessagingAvailability(conversation);
        return persistAndBroadcastMessage(
                conversation,
                request.senderId(),
                request.senderName().trim(),
                request.content().trim(),
                null
        );
    }

    @Transactional
    public ChatMessageResponse sendImageMessage(Long conversationId, Long senderId, String senderName, String content, org.springframework.web.multipart.MultipartFile image) {
        Validators.requirePositive(conversationId, "conversationId");
        Validators.requirePositive(senderId, "senderId");
        Validators.requireImage(image, Validators.MAX_IMAGE_BYTES);
        ChatConversation conversation = findConversation(conversationId);
        validateParticipant(conversation, senderId);
        validateMessagingAvailability(conversation);

        CoverUploadResponse upload = storageGatewayService.uploadChatImage(image, senderId);
        return persistAndBroadcastMessage(
                conversation,
                senderId,
                senderName == null ? "Student" : senderName.trim(),
                content == null ? "" : content.trim(),
                upload.imageUrl()
        );
    }

    @Transactional
    public ChatMessageResponse updateMessage(Long messageId, ChatMessageUpdateRequest request) {
        Validators.requirePositive(messageId, "messageId");
        Validators.requireNonNull(request, "request");
        Validators.requirePositive(request.participantId(), "participantId");
        ChatMessage message = findMessage(messageId);
        ChatConversation conversation = findConversation(message.getConversationId());
        validateParticipant(conversation, request.participantId());
        validateMessageOwner(message, request.participantId());

        if (message.getDeletedAt() != null) {
            throw new ConflictException("Deleted messages cannot be edited.");
        }

        message.setContent(request.content().trim());
        message.setEditedAt(Instant.now());
        ChatMessage savedMessage = chatMessageRepository.save(message);
        broadcastMessageEvent(savedMessage);
        return chatMessageMapper.toResponse(savedMessage);
    }

    @Transactional
    public ChatMessageResponse deleteMessage(Long messageId, Long participantId) {
        Validators.requirePositive(messageId, "messageId");
        Validators.requirePositive(participantId, "participantId");
        ChatMessage message = findMessage(messageId);
        ChatConversation conversation = findConversation(message.getConversationId());
        validateParticipant(conversation, participantId);
        validateMessageOwner(message, participantId);

        if (message.getDeletedAt() == null) {
            message.setContent("");
            message.setImageUrl(null);
            message.setDeletedAt(Instant.now());
        }

        ChatMessage savedMessage = chatMessageRepository.save(message);
        broadcastMessageEvent(savedMessage);
        return chatMessageMapper.toResponse(savedMessage);
    }

    public ChatTypingResponse publishTypingIndicator(ChatTypingRequest request) {
        Validators.requireNonNull(request, "request");
        Validators.requirePositive(request.conversationId(), "conversationId");
        Validators.requirePositive(request.senderId(), "senderId");
        ChatConversation conversation = findConversation(request.conversationId());
        validateParticipant(conversation, request.senderId());
        if (!conversation.isActive()) {
            throw new ConflictException("This conversation is not active.");
        }

        ChatTypingResponse response = new ChatTypingResponse(
                conversation.getId(),
                request.senderId(),
                request.senderName().trim(),
                Boolean.TRUE.equals(request.typing()),
                Instant.now()
        );

        messagingTemplate.convertAndSend(
                "/topic/student-services/chat/" + conversation.getId() + "/typing",
                response
        );
        return response;
    }

    @Transactional(readOnly = true)
    public ChatConversation findConversation(Long conversationId) {
        Validators.requirePositive(conversationId, "conversationId");
        return chatConversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found."));
    }

    private void validateParticipant(ChatConversation conversation, Long participantId) {
        if (participantId == null) {
            return;
        }

        boolean allowed = participantId.equals(conversation.getRequesterId())
                || participantId.equals(conversation.getProviderId());
        if (!allowed) {
            throw new ConflictException("You do not have access to this conversation.");
        }
    }

    private ConversationResponse toConversationResponse(ChatConversation conversation, Long currentUserId) {
        ChatMessage lastMessage = chatMessageRepository.findTopByConversationIdOrderBySentAtDesc(conversation.getId());
        boolean requesterPerspective = currentUserId != null && currentUserId.equals(conversation.getRequesterId());

        return new ConversationResponse(
                conversation.getId(),
                conversation.getRequestId(),
                conversation.getServiceId(),
                conversation.getServiceTitle(),
                conversation.getRequesterId(),
                conversation.getRequesterName(),
                conversation.getProviderId(),
                conversation.getProviderName(),
                requesterPerspective ? conversation.getProviderId() : conversation.getRequesterId(),
                requesterPerspective ? conversation.getProviderName() : conversation.getRequesterName(),
                conversation.isActive(),
                lastMessage != null ? buildConversationPreview(lastMessage) : "No messages yet.",
                lastMessage != null ? lastMessage.getSentAt() : null,
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    private ChatMessageResponse persistAndBroadcastMessage(
            ChatConversation conversation,
            Long senderId,
            String senderName,
            String content,
            String imageUrl
    ) {
        if ((content == null || content.isBlank()) && (imageUrl == null || imageUrl.isBlank())) {
            throw new ConflictException("A message must contain text or an image.");
        }

        ChatMessage savedMessage = chatMessageRepository.save(ChatMessage.builder()
                .conversationId(conversation.getId())
                .senderId(senderId)
                .senderName(senderName)
                .content(content == null ? "" : content)
                .imageUrl(imageUrl)
                .sentAt(Instant.now())
                .build());

        conversation.setUpdatedAt(savedMessage.getSentAt());
        chatConversationRepository.save(conversation);
        broadcastMessageEvent(savedMessage);
        return chatMessageMapper.toResponse(savedMessage);
    }

    private void validateMessagingAvailability(ChatConversation conversation) {
        if (!conversation.isActive()) {
            throw new ConflictException("This conversation is not active.");
        }

        ServiceRequest linkedRequest = serviceRequestRepository.findById(conversation.getRequestId())
                .orElseThrow(() -> new NotFoundException("Linked service request not found."));
        if (linkedRequest.getStatus() != ServiceRequestStatus.ACCEPTED && linkedRequest.getStatus() != ServiceRequestStatus.COMPLETED) {
            throw new ConflictException("Messaging is only available after the request is accepted.");
        }
    }

    private ChatMessage findMessage(Long messageId) {
        return chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found."));
    }

    private void validateMessageOwner(ChatMessage message, Long participantId) {
        if (participantId == null || !participantId.equals(message.getSenderId())) {
            throw new ConflictException("Only the sender can modify this message.");
        }
    }

    private void broadcastMessageEvent(ChatMessage message) {
        messagingTemplate.convertAndSend(
                "/topic/student-services/chat/" + message.getConversationId(),
                chatMessageMapper.toResponse(message)
        );
    }

    private String buildConversationPreview(ChatMessage message) {
        if (message.getDeletedAt() != null) {
            return "Message removed";
        }
        if (message.getImageUrl() != null && !message.getImageUrl().isBlank()) {
            return message.getContent() != null && !message.getContent().isBlank()
                    ? message.getContent()
                    : "Sent an image";
        }
        return message.getContent();
    }
}
