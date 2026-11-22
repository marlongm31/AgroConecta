package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.in.ProductionUseCase;
import com.agroconecta.application.usecase.IdempotencyService;
import com.agroconecta.domain.model.ProductionRecord;
import com.agroconecta.infrastructure.adapter.in.security.AuthenticatedUser;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.ApiResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.PageResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.ProductionDtos.ProductionRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.ProductionDtos.ProductionResponse;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/production")
@Tag(name = "Producción", description = "Registro y administración de cosechas")
public class ProductionController {

    private final ProductionUseCase productionUseCase;
    private final IdempotencyService idempotencyService;

    public ProductionController(ProductionUseCase productionUseCase, IdempotencyService idempotencyService) {
        this.productionUseCase = productionUseCase;
        this.idempotencyService = idempotencyService;
    }

    @GetMapping
    @Operation(summary = "Listar registros de producción del productor")
    public ResponseEntity<ApiResponse<PageResponse<ProductionResponse>>> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<ProductionRecord> result = productionUseCase.list(user.id(), PageQuery.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result, ProductionResponse::from)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un registro de producción propio")
    public ResponseEntity<ApiResponse<ProductionResponse>> get(@AuthenticationPrincipal AuthenticatedUser user,
                                                               @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ProductionResponse.from(productionUseCase.get(user.id(), id))));
    }

    @PostMapping
    @Operation(summary = "Registrar una cosecha")
    public ResponseEntity<ApiResponse<ProductionResponse>> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ProductionRequest request) {
        ProductionRecord created = idempotencyService.execute(user.id(), idempotencyKey, "PRODUCTION",
                id -> Optional.of(productionUseCase.get(user.id(), id)),
                () -> productionUseCase.create(user.id(), request.toCommand()), ProductionRecord::id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ProductionResponse.from(created)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un registro de producción propio")
    public ResponseEntity<ApiResponse<ProductionResponse>> update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody ProductionRequest request) {
        ProductionRecord updated = productionUseCase.update(user.id(), id, request.toCommand());
        return ResponseEntity.ok(ApiResponse.message("Registro de producción actualizado",
                ProductionResponse.from(updated)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un registro de producción propio")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable Long id) {
        productionUseCase.delete(user.id(), id);
        return ResponseEntity.ok(ApiResponse.message("Registro de producción eliminado correctamente", null));
    }
}
