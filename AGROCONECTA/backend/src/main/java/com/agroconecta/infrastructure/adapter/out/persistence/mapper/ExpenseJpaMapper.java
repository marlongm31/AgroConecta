package com.agroconecta.infrastructure.adapter.out.persistence.mapper;

import com.agroconecta.domain.model.Expense;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.ExpenseJpaEntity;

public final class ExpenseJpaMapper {

    private ExpenseJpaMapper() {
    }

    public static Expense toDomain(ExpenseJpaEntity entity) {
        return new Expense(
                entity.getId(),
                entity.getOwnerId(),
                entity.getParcelId(),
                entity.getCropId(),
                entity.getConcept(),
                entity.getAmount(),
                entity.getExpenseDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ExpenseJpaEntity toEntity(Expense expense) {
        ExpenseJpaEntity entity = new ExpenseJpaEntity();
        entity.setId(expense.id());
        entity.setOwnerId(expense.ownerId());
        entity.setParcelId(expense.parcelId());
        entity.setCropId(expense.cropId());
        entity.setConcept(expense.concept());
        entity.setAmount(expense.amount());
        entity.setExpenseDate(expense.expenseDate());
        entity.setCreatedAt(expense.createdAt());
        entity.setUpdatedAt(expense.updatedAt());
        return entity;
    }
}
