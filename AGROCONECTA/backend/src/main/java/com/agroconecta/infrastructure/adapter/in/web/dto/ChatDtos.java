package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.application.model.ChatConversation;
import com.agroconecta.application.model.ChatMessage;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class ChatDtos {

    private ChatDtos() {
    }

    public record StartConversationRequest(@NotNull Long productId) {
    }

    public record SendMessageRequest(
            @Size(max = 2000, message = "El mensaje no puede superar 2000 caracteres")
            String content,
            Long replyToMessageId
    ) {
    }

    public record ConversationResponse(
            Long id,
            Long productId,
            String productName,
            Long counterpartId,
            String counterpartName,
            String counterpartAvatarUrl,
            String lastMessage,
            Instant lastMessageAt,
            long unreadCount
    ) {

        public static ConversationResponse from(ChatConversation conversation) {
            return new ConversationResponse(conversation.id(), conversation.productId(), conversation.productName(),
                    conversation.counterpartId(), conversation.counterpartName(),
                    conversation.counterpartAvatarUrl(), conversation.lastMessage(),
                    conversation.lastMessageAt(), conversation.unreadCount());
        }
    }

    public record AttachmentResponse(
            Long id,
            String name,
            String contentType,
            Long sizeBytes,
            String downloadUrl
    ) {

        public static AttachmentResponse from(ChatMessage.Attachment attachment) {
            return new AttachmentResponse(attachment.id(), attachment.name(), attachment.contentType(),
                    attachment.sizeBytes(), attachment.downloadUrl());
        }
    }

    public record MessageResponse(
            Long id,
            Long conversationId,
            Long senderId,
            String senderName,
            String senderAvatarUrl,
            String content,
            String clientMessageId,
            AttachmentResponse attachment,
            Instant createdAt,
            Instant readAt,
            boolean mine,
            Long replyToMessageId
    ) {

        public static MessageResponse from(ChatMessage message) {
            return new MessageResponse(message.id(), message.conversationId(), message.senderId(),
                    message.senderName(), message.senderAvatarUrl(), message.content(), message.clientMessageId(),
                    message.attachment() == null ? null : AttachmentResponse.from(message.attachment()),
                    message.createdAt(), message.readAt(), message.mine(), message.replyToMessageId());
        }
    }

    public record UnreadCountResponse(long count) {
    }
}
