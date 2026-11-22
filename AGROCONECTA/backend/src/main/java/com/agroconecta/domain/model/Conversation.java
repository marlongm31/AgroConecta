package com.agroconecta.domain.model;

import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.validation.DomainValidator;

import java.time.Instant;
import java.util.Objects;

public record Conversation(
        Long id,
        Long productId,
        Long buyerId,
        Long producerId,
        Instant lastMessageAt,
        Instant createdAt,
        Instant updatedAt
) {

    public Conversation {
        DomainValidator.requireId(buyerId, "comprador");
        DomainValidator.requireId(producerId, "productor");
        if (Objects.equals(buyerId, producerId)) {
            throw new BusinessRuleException("No puedes iniciar una conversación contigo mismo");
        }
    }

    public static Conversation create(Long productId, Long buyerId, Long producerId) {
        Instant now = Instant.now();
        return new Conversation(null, productId, buyerId, producerId, now, now, now);
    }

    public boolean includes(Long userId) {
        return Objects.equals(buyerId, userId) || Objects.equals(producerId, userId);
    }

    public Long counterpartOf(Long userId) {
        return Objects.equals(buyerId, userId) ? producerId : buyerId;
    }

    public Conversation touch(Instant instant) {
        return new Conversation(id, productId, buyerId, producerId, instant, createdAt, instant);
    }
}
