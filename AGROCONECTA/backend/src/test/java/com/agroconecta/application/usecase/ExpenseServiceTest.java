package com.agroconecta.application.usecase;

import com.agroconecta.application.command.ExpenseCommand;
import com.agroconecta.application.port.out.CropRepositoryPort;
import com.agroconecta.application.port.out.ExpenseRepositoryPort;
import com.agroconecta.application.port.out.ParcelRepositoryPort;
import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.model.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseService: gastos y control de relaciones")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepositoryPort expenseRepository;
    @Mock
    private ParcelRepositoryPort parcelRepository;
    @Mock
    private CropRepositoryPort cropRepository;

    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseService = new ExpenseService(expenseRepository, parcelRepository, cropRepository);
    }

    @Test
    @DisplayName("Exige una parcela o un cultivo asociado")
    void requiresRelation() {
        ExpenseCommand command = new ExpenseCommand(null, null, "Insumos", new BigDecimal("1000"), LocalDate.now());

        assertThatThrownBy(() -> expenseService.create(2L, command))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Impide registrar un gasto sobre una parcela ajena")
    void forbidsForeignParcel() {
        ExpenseCommand command = new ExpenseCommand(9L, null, "Insumos", new BigDecimal("1000"), LocalDate.now());
        when(parcelRepository.belongsToOwner(9L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> expenseService.create(2L, command))
                .isInstanceOf(ForbiddenException.class);
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    @DisplayName("Registra un gasto sobre una parcela propia")
    void registersExpense() {
        ExpenseCommand command = new ExpenseCommand(1L, null, "Fertilizante", new BigDecimal("185000"), LocalDate.now());
        when(parcelRepository.belongsToOwner(1L, 2L)).thenReturn(true);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense expense = expenseService.create(2L, command);

        assertThat(expense.ownerId()).isEqualTo(2L);
        assertThat(expense.amount()).isEqualByComparingTo("185000");
    }

    @Test
    @DisplayName("Impide acceder a gastos de otro productor")
    void forbidsForeignExpense() {
        Expense expense = new Expense(1L, 5L, 1L, null, "Insumos", BigDecimal.TEN, LocalDate.now(), null, null);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> expenseService.get(2L, 1L))
                .isInstanceOf(ForbiddenException.class);
    }
}
