package com.agroconecta.application.port.out;

import com.agroconecta.domain.model.Message;

import java.util.List;
import java.util.Optional;

public interface MessageRepositoryPort {

    Message save(Message message);

    Optional<Message> findById(Long id);

    List<Message> findRecent(Long conversationId, int limit);

    List<Message> findAfter(Long conversationId, Long afterId);

    Optional<Message> findLast(Long conversationId);

    long countUnreadForParticipant(Long userId);

    long countUnreadInConversation(Long conversationId, Long readerId);

    void markConversationRead(Long conversationId, Long readerId);
}
