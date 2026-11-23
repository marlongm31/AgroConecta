package com.agroconecta.infrastructure.adapter.out.persistence.mapper;

import com.agroconecta.domain.model.Conversation;
import com.agroconecta.domain.model.Message;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.ConversationJpaEntity;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.MessageJpaEntity;

public final class ChatJpaMapper {

    private ChatJpaMapper() {
    }

    public static Conversation toDomain(ConversationJpaEntity entity) {
        return new Conversation(entity.getId(), entity.getProductId(), entity.getBuyerId(), entity.getProducerId(),
                entity.getLastMessageAt(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public static ConversationJpaEntity toEntity(Conversation conversation) {
        ConversationJpaEntity entity = new ConversationJpaEntity();
        entity.setId(conversation.id());
        entity.setProductId(conversation.productId());
        entity.setBuyerId(conversation.buyerId());
        entity.setProducerId(conversation.producerId());
        entity.setLastMessageAt(conversation.lastMessageAt());
        entity.setCreatedAt(conversation.createdAt());
        entity.setUpdatedAt(conversation.updatedAt());
        return entity;
    }

    public static Message toDomain(MessageJpaEntity entity) {
        return new Message(entity.getId(), entity.getConversationId(), entity.getSenderId(), entity.getContent(),
                entity.getClientMessageId(), entity.getAttachmentUrl(), entity.getAttachmentContentType(),
                entity.getAttachmentSizeBytes(), entity.getAttachmentName(), entity.getCreatedAt(), entity.getReadAt(),
                entity.getReplyToMessageId());
    }

    public static MessageJpaEntity toEntity(Message message) {
        MessageJpaEntity entity = new MessageJpaEntity();
        entity.setId(message.id());
        entity.setConversationId(message.conversationId());
        entity.setSenderId(message.senderId());
        entity.setContent(message.content());
        entity.setClientMessageId(message.clientMessageId());
        entity.setAttachmentUrl(message.attachmentUrl());
        entity.setAttachmentContentType(message.attachmentContentType());
        entity.setAttachmentSizeBytes(message.attachmentSizeBytes());
        entity.setAttachmentName(message.attachmentName());
        entity.setCreatedAt(message.createdAt());
        entity.setReadAt(message.readAt());
        entity.setReplyToMessageId(message.replyToMessageId());
        return entity;
    }
}
