package com.agroconecta.application.port.in;

import com.agroconecta.application.command.ExpenseCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.Expense;

public interface ExpenseUseCase {

    PageResult<Expense> list(Long ownerId, PageQuery pageQuery);

    Expense get(Long ownerId, Long expenseId);

    Expense create(Long ownerId, ExpenseCommand command);

    Expense update(Long ownerId, Long expenseId, ExpenseCommand command);

    void delete(Long ownerId, Long expenseId);
}
