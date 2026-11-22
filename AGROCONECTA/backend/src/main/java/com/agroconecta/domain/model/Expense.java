package com.agroconecta.domain.model;

import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.validation.DomainValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public record Expense(
        Long id,
        Long ownerId,
        Long parcelId,
        Long cropId,
        String concept,
        BigDecimal amount,
        LocalDate expenseDate,
        Instant createdAt,
        Instant updatedAt
) {

    public Expense {
        Objects.requireNonNull(ownerId, "El propietario es obligatorio");
        if (parcelId == null && cropId == null) {
            throw new BusinessRuleException("El gasto debe estar asociado a una parcela o a un cultivo");
        }
        concept = DomainValidator.requiredText(concept, "concepto");
        amount = DomainValidator.positive(amount, "valor");
        Objects.requireNonNull(expenseDate, "La fecha del gasto es obligatoria");
    }

    public static Expense create(Long ownerId, Long parcelId, Long cropId, String concept,
                                 BigDecimal amount, LocalDate expenseDate) {
        return new Expense(null, ownerId, parcelId, cropId, concept, amount, expenseDate, null, null);
    }

    public Expense update(Long parcelId, Long cropId, String concept, BigDecimal amount, LocalDate expenseDate) {
        return new Expense(id, ownerId, parcelId, cropId, concept, amount, expenseDate, createdAt, updatedAt);
    }
}
