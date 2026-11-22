package com.agroconecta.application.model;

import java.time.Instant;

public record ChatConversation(
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
}
