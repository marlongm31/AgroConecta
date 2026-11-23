package com.agroconecta.infrastructure.adapter.out.persistence.repository;

import com.agroconecta.infrastructure.adapter.out.persistence.entity.ConversationJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationJpaRepository extends JpaRepository<ConversationJpaEntity, Long> {

    Optional<ConversationJpaEntity> findByProductIdAndBuyerId(Long productId, Long buyerId);

    Page<ConversationJpaEntity> findByBuyerIdOrProducerIdOrderByLastMessageAtDesc(Long buyerId, Long producerId,
                                                                                  Pageable pageable);
}
