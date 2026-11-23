package com.agroconecta.infrastructure.adapter.out.persistence;

import com.agroconecta.application.port.out.ProcessedOperationRepositoryPort;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.ProcessedOperationJpaEntity;
import com.agroconecta.infrastructure.adapter.out.persistence.repository.ProcessedOperationJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProcessedOperationPersistenceAdapter implements ProcessedOperationRepositoryPort {

    private final ProcessedOperationJpaRepository repository;

    public ProcessedOperationPersistenceAdapter(ProcessedOperationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Long> findResourceId(Long ownerId, String operationKey) {
        return repository.findByOwnerIdAndOperationKey(ownerId, operationKey)
                .map(ProcessedOperationJpaEntity::getResourceId);
    }

    @Override
    public boolean register(Long ownerId, String operationKey, String resourceType, Long resourceId) {
        ProcessedOperationJpaEntity entity = new ProcessedOperationJpaEntity();
        entity.setOwnerId(ownerId);
        entity.setOperationKey(operationKey);
        entity.setResourceType(resourceType);
        entity.setResourceId(resourceId);
        try {
            repository.saveAndFlush(entity);
            return true;
        } catch (DataIntegrityViolationException exception) {
            return false;
        }
    }
}
