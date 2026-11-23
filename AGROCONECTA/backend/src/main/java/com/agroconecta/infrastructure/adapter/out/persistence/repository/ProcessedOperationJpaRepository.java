package com.agroconecta.infrastructure.adapter.out.persistence.repository;

import com.agroconecta.infrastructure.adapter.out.persistence.entity.ProcessedOperationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessedOperationJpaRepository extends JpaRepository<ProcessedOperationJpaEntity, Long> {

    Optional<ProcessedOperationJpaEntity> findByOwnerIdAndOperationKey(Long ownerId, String operationKey);
}
