package com.agroconecta.infrastructure.adapter.out.persistence;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.out.SoilAssessmentRepositoryPort;
import com.agroconecta.domain.model.SoilAssessment;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.SoilAssessmentJpaEntity;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.SoilAssessmentJpaMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.repository.SoilAssessmentJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SoilAssessmentPersistenceAdapter implements SoilAssessmentRepositoryPort {

    private final SoilAssessmentJpaRepository repository;

    public SoilAssessmentPersistenceAdapter(SoilAssessmentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public SoilAssessment save(SoilAssessment assessment) {
        return SoilAssessmentJpaMapper.toDomain(
                repository.save(SoilAssessmentJpaMapper.toEntity(assessment)));
    }

    @Override
    public Optional<SoilAssessment> findByIdAndOwnerId(Long id, Long ownerId) {
        return repository.findByIdAndOwnerId(id, ownerId).map(SoilAssessmentJpaMapper::toDomain);
    }

    @Override
    public PageResult<SoilAssessment> findByOwnerId(Long ownerId, PageQuery pageQuery) {
        Page<SoilAssessmentJpaEntity> page = repository.findByOwnerIdOrderByCreatedAtDesc(ownerId,
                PageRequest.of(pageQuery.page(), pageQuery.size()));
        return new PageResult<>(page.getContent().stream().map(SoilAssessmentJpaMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
