package com.agroconecta.infrastructure.adapter.out.persistence.repository;

import com.agroconecta.infrastructure.adapter.out.persistence.entity.MessageJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, Long> {

    List<MessageJpaEntity> findByConversationIdOrderByCreatedAtAscIdAsc(Long conversationId, Pageable pageable);

    List<MessageJpaEntity> findByConversationIdAndIdGreaterThanOrderByIdAsc(Long conversationId, Long afterId);

    Optional<MessageJpaEntity> findFirstByConversationIdOrderByCreatedAtDescIdDesc(Long conversationId);

    long countByConversationIdAndSenderIdNotAndReadAtIsNull(Long conversationId, Long senderId);

    @Query("""
            select count(m) from MessageJpaEntity m, ConversationJpaEntity c
            where m.conversationId = c.id
              and (c.buyerId = :userId or c.producerId = :userId)
              and m.senderId <> :userId
              and m.readAt is null
            """)
    long countUnreadForParticipant(@Param("userId") Long userId);

    @Modifying
    @Query("""
            update MessageJpaEntity m
            set m.readAt = :readAt
            where m.conversationId = :conversationId
              and m.senderId <> :readerId
              and m.readAt is null
            """)
    int markConversationRead(@Param("conversationId") Long conversationId,
                             @Param("readerId") Long readerId,
                             @Param("readAt") Instant readAt);
}
