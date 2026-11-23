package com.agroconecta.infrastructure.adapter.out.persistence;

import com.agroconecta.application.port.out.MessageRepositoryPort;
import com.agroconecta.domain.model.Message;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.ChatJpaMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.repository.MessageJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class MessagePersistenceAdapter implements MessageRepositoryPort {

    private final MessageJpaRepository repository;

    public MessagePersistenceAdapter(MessageJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Message save(Message message) {
        return ChatJpaMapper.toDomain(repository.save(ChatJpaMapper.toEntity(message)));
    }

    @Override
    public Optional<Message> findById(Long id) {
        return repository.findById(id).map(ChatJpaMapper::toDomain);
    }

    @Override
    public List<Message> findRecent(Long conversationId, int limit) {
        return repository.findByConversationIdOrderByCreatedAtAscIdAsc(conversationId,
                        PageRequest.of(0, limit, Sort.by("createdAt", "id"))).stream()
                .map(ChatJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Message> findAfter(Long conversationId, Long afterId) {
        return repository.findByConversationIdAndIdGreaterThanOrderByIdAsc(conversationId, afterId).stream()
                .map(ChatJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Message> findLast(Long conversationId) {
        return repository.findFirstByConversationIdOrderByCreatedAtDescIdDesc(conversationId)
                .map(ChatJpaMapper::toDomain);
    }

    @Override
    public long countUnreadForParticipant(Long userId) {
        return repository.countUnreadForParticipant(userId);
    }

    @Override
    public long countUnreadInConversation(Long conversationId, Long readerId) {
        return repository.countByConversationIdAndSenderIdNotAndReadAtIsNull(conversationId, readerId);
    }

    @Override
    @Transactional
    public void markConversationRead(Long conversationId, Long readerId) {
        repository.markConversationRead(conversationId, readerId, Instant.now());
    }
}
