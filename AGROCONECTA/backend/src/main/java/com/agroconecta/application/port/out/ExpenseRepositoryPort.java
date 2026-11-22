package com.agroconecta.application.port.out;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.Expense;

import java.math.BigDecimal;
import java.util.Optional;

public interface ExpenseRepositoryPort {

    Optional<Expense> findById(Long id);

    Expense save(Expense expense);

    void deleteById(Long id);

    PageResult<Expense> findByOwnerId(Long ownerId, PageQuery pageQuery);

    BigDecimal sumAmountByOwnerId(Long ownerId);

    long countByOwnerId(Long ownerId);
}
