package com.agroconecta.application.model;

import java.time.Instant;

public record ChatMessage(
        Long id,
        Long conversationId,
        Long senderId,
        String senderName,
        String senderAvatarUrl,
        String content,
        String clientMessageId,
        Attachment attachment,
        Instant createdAt,
        Instant readAt,
        boolean mine,
        Long replyToMessageId
) {

    public record Attachment(Long id, String name, String contentType, Long sizeBytes, String downloadUrl) {
    }
}
