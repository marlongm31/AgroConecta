package com.agroconecta.infrastructure.adapter.out.persistence.repository;

import com.agroconecta.infrastructure.adapter.out.persistence.entity.ParcelJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcelJpaRepository extends JpaRepository<ParcelJpaEntity, Long> {

    Page<ParcelJpaEntity> findByOwnerId(Long ownerId, Pageable pageable);

    long countByOwnerId(Long ownerId);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);
}
