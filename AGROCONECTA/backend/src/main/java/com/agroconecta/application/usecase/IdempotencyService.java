package com.agroconecta.application.usecase;

import com.agroconecta.application.port.out.ProcessedOperationRepositoryPort;
import com.agroconecta.domain.exception.ConflictException;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public class IdempotencyService {

    private final ProcessedOperationRepositoryPort repository;

    public IdempotencyService(ProcessedOperationRepositoryPort repository) {
        this.repository = repository;
    }

    public <T> T execute(Long ownerId, String operationKey, String resourceType,
                         LongFunction<Optional<T>> findExisting, Supplier<T> creator, Function<T, Long> idOf) {
        String key = normalize(operationKey);
        if (key == null) {
            return creator.get();
        }
        Optional<Long> existingId = repository.findResourceId(ownerId, key);
        if (existingId.isPresent()) {
            return findExisting.apply(existingId.get())
                    .orElseThrow(() -> new ConflictException(
                            "La operación ya fue procesada pero el registro no está disponible"));
        }
        T created = creator.get();
        boolean registered = repository.register(ownerId, key, resourceType, idOf.apply(created));
        if (registered) {
            return created;
        }
        Long concurrentId = repository.findResourceId(ownerId, key)
                .orElseThrow(() -> new ConflictException(
                        "La operación se está sincronizando. Intenta de nuevo en unos segundos"));
        return findExisting.apply(concurrentId)
                .orElseThrow(() -> new ConflictException("La operación ya fue procesada"));
    }

    private String normalize(String operationKey) {
        if (operationKey == null || operationKey.isBlank()) {
            return null;
        }
        String key = operationKey.trim();
        return key.length() > 120 ? key.substring(0, 120) : key;
    }
}
