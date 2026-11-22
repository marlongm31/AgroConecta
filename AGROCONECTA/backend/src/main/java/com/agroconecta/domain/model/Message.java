package com.agroconecta.domain.model;

import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.validation.DomainValidator;

import java.time.Instant;

public record Message(
        Long id,
        Long conversationId,
        Long senderId,
        String content,
        String clientMessageId,
        String attachmentUrl,
        String attachmentContentType,
        Long attachmentSizeBytes,
        String attachmentName,
        Instant createdAt,
        Instant readAt,
        Long replyToMessageId
) {

    public record Attachment(String url, String contentType, Long sizeBytes, String name) {
    }

    public Message {
        DomainValidator.requireId(conversationId, "conversación");
        DomainValidator.requireId(senderId, "remitente");
        content = DomainValidator.optionalText(content, 2000, "mensaje");
        clientMessageId = DomainValidator.optionalText(clientMessageId, 120, "identificador del mensaje");
        attachmentName = DomainValidator.optionalText(attachmentName, 120, "nombre del archivo");
        if ((content == null || content.isBlank()) && attachmentUrl == null) {
            throw new BusinessRuleException("Escribe un mensaje o adjunta un archivo");
        }
        if ((content == null || content.isBlank()) && attachmentUrl != null) {
            content = null;
        }
        if (attachmentUrl != null
                && (attachmentContentType == null || attachmentSizeBytes == null || attachmentName == null)) {
            throw new BusinessRuleException("Los datos del archivo adjunto no son válidos");
        }
        if (replyToMessageId != null && replyToMessageId <= 0) {
            throw new BusinessRuleException("La referencia al mensaje respondido no es válida");
        }
    }

    public static Message create(Long conversationId, Long senderId, String content) {
        return create(conversationId, senderId, content, null, null, null);
    }

    public static Message create(Long conversationId, Long senderId, String content,
                                 String clientMessageId, Attachment attachment) {
        return create(conversationId, senderId, content, clientMessageId, attachment, null);
    }

    public static Message create(Long conversationId, Long senderId, String content,
                                 String clientMessageId, Attachment attachment, Long replyToMessageId) {
        return new Message(null, conversationId, senderId, content, clientMessageId,
                attachment == null ? null : attachment.url(),
                attachment == null ? null : attachment.contentType(),
                attachment == null ? null : attachment.sizeBytes(),
                attachment == null ? null : attachment.name(),
                Instant.now(), null, replyToMessageId);
    }
}
