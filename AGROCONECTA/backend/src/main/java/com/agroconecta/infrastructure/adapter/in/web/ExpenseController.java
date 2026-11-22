package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.in.ExpenseUseCase;
import com.agroconecta.domain.model.Expense;
import com.agroconecta.infrastructure.adapter.in.security.AuthenticatedUser;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.ApiResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.PageResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.ExpenseDtos.ExpenseRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.ExpenseDtos.ExpenseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
@Tag(name = "Gastos", description = "Control de gastos asociados a parcelas o cultivos")
public class ExpenseController {

    private final ExpenseUseCase expenseUseCase;

    public ExpenseController(ExpenseUseCase expenseUseCase) {
        this.expenseUseCase = expenseUseCase;
    }

    @GetMapping
    @Operation(summary = "Listar gastos del productor autenticado")
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Expense> result = expenseUseCase.list(user.id(), PageQuery.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result, ExpenseResponse::from)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un gasto propio")
    public ResponseEntity<ApiResponse<ExpenseResponse>> get(@AuthenticationPrincipal AuthenticatedUser user,
                                                            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ExpenseResponse.from(expenseUseCase.get(user.id(), id))));
    }

    @PostMapping
    @Operation(summary = "Registrar un gasto")
    public ResponseEntity<ApiResponse<ExpenseResponse>> create(@AuthenticationPrincipal AuthenticatedUser user,
                                                               @Valid @RequestBody ExpenseRequest request) {
        Expense created = expenseUseCase.create(user.id(), request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ExpenseResponse.from(created)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un gasto propio")
    public ResponseEntity<ApiResponse<ExpenseResponse>> update(@AuthenticationPrincipal AuthenticatedUser user,
                                                               @PathVariable Long id,
                                                               @Valid @RequestBody ExpenseRequest request) {
        Expense updated = expenseUseCase.update(user.id(), id, request.toCommand());
        return ResponseEntity.ok(ApiResponse.message("Gasto actualizado correctamente", ExpenseResponse.from(updated)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un gasto propio")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable Long id) {
        expenseUseCase.delete(user.id(), id);
        return ResponseEntity.ok(ApiResponse.message("Gasto eliminado correctamente", null));
    }
}
