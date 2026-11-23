package com.agroconecta.infrastructure.adapter.out.persistence.repository;

import com.agroconecta.infrastructure.adapter.out.persistence.entity.ExpenseJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseJpaEntity, Long> {

    Page<ExpenseJpaEntity> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("select coalesce(sum(e.amount), 0) from ExpenseJpaEntity e where e.ownerId = :ownerId")
    BigDecimal sumAmountByOwnerId(@Param("ownerId") Long ownerId);

    long countByOwnerId(Long ownerId);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);
}
