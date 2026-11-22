package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.application.command.ExpenseCommand;
import com.agroconecta.domain.model.Expense;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class ExpenseDtos {

    private ExpenseDtos() {
    }

    public record ExpenseRequest(
            Long parcelId,
            Long cropId,
            @NotBlank(message = "El concepto es obligatorio")
            @Size(max = 180, message = "El concepto no puede superar 180 caracteres")
            String concept,
            @NotNull(message = "El valor es obligatorio")
            @DecimalMin(value = "0.01", message = "El valor debe ser mayor que cero")
            @Digits(integer = 12, fraction = 2, message = "El valor admite máximo 2 decimales")
            BigDecimal amount,
            @NotNull(message = "La fecha es obligatoria")
            LocalDate expenseDate
    ) {

        public ExpenseCommand toCommand() {
            return new ExpenseCommand(parcelId, cropId, concept, amount, expenseDate);
        }
    }

    public record ExpenseResponse(
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

        public static ExpenseResponse from(Expense expense) {
            return new ExpenseResponse(expense.id(), expense.ownerId(), expense.parcelId(), expense.cropId(),
                    expense.concept(), expense.amount(), expense.expenseDate(), expense.createdAt(),
                    expense.updatedAt());
        }
    }
}
