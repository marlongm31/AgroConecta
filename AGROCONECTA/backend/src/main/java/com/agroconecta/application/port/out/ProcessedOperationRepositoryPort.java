package com.agroconecta.application.port.out;

import java.util.Optional;

public interface ProcessedOperationRepositoryPort {

    Optional<Long> findResourceId(Long ownerId, String operationKey);

    boolean register(Long ownerId, String operationKey, String resourceType, Long resourceId);
}
