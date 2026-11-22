package com.agroconecta.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseCommand(
        Long parcelId,
        Long cropId,
        String concept,
        BigDecimal amount,
        LocalDate expenseDate
) {
}
