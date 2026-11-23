package com.agroconecta.infrastructure.adapter.out.persistence.repository;

import com.agroconecta.infrastructure.adapter.out.persistence.entity.SoilAssessmentJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SoilAssessmentJpaRepository extends JpaRepository<SoilAssessmentJpaEntity, Long> {

    Page<SoilAssessmentJpaEntity> findByOwnerIdOrderByCreatedAtDesc(Long ownerId, Pageable pageable);

    Optional<SoilAssessmentJpaEntity> findByIdAndOwnerId(Long id, Long ownerId);
}
