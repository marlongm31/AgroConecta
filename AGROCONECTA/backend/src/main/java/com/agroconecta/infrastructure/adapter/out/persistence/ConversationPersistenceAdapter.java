package com.agroconecta.infrastructure.adapter.out.persistence;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.out.ConversationRepositoryPort;
import com.agroconecta.domain.model.Conversation;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.ConversationJpaEntity;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.ChatJpaMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.repository.ConversationJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConversationPersistenceAdapter implements ConversationRepositoryPort {

    private final ConversationJpaRepository repository;

    public ConversationPersistenceAdapter(ConversationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Conversation> findById(Long id) {
        return repository.findById(id).map(ChatJpaMapper::toDomain);
    }

    @Override
    public Optional<Conversation> findByProductIdAndBuyerId(Long productId, Long buyerId) {
        return repository.findByProductIdAndBuyerId(productId, buyerId).map(ChatJpaMapper::toDomain);
    }

    @Override
    public Conversation save(Conversation conversation) {
        return ChatJpaMapper.toDomain(repository.save(ChatJpaMapper.toEntity(conversation)));
    }

    @Override
    public PageResult<Conversation> findByParticipant(Long userId, PageQuery pageQuery) {
        Page<ConversationJpaEntity> page = repository.findByBuyerIdOrProducerIdOrderByLastMessageAtDesc(userId,
                userId, PageRequest.of(pageQuery.page(), pageQuery.size()));
        return new PageResult<>(page.getContent().stream().map(ChatJpaMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
