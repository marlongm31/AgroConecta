package com.agroconecta.application.port.out;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.Conversation;

import java.util.Optional;

public interface ConversationRepositoryPort {

    Optional<Conversation> findById(Long id);

    Optional<Conversation> findByProductIdAndBuyerId(Long productId, Long buyerId);

    Conversation save(Conversation conversation);

    PageResult<Conversation> findByParticipant(Long userId, PageQuery pageQuery);
}
