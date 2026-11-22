package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.in.CropUseCase;
import com.agroconecta.application.usecase.IdempotencyService;
import com.agroconecta.domain.model.Crop;
import com.agroconecta.infrastructure.adapter.in.security.AuthenticatedUser;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.ApiResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.PageResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.CropDtos.ChangeCropStatusRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.CropDtos.CropRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.CropDtos.CropResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/crops")
@Tag(name = "Cultivos", description = "Gestión de cultivos asociados a parcelas")
public class CropController {

    private final CropUseCase cropUseCase;
    private final IdempotencyService idempotencyService;

    public CropController(CropUseCase cropUseCase, IdempotencyService idempotencyService) {
        this.cropUseCase = cropUseCase;
        this.idempotencyService = idempotencyService;
    }

    @GetMapping
    @Operation(summary = "Listar cultivos del productor, opcionalmente por parcela")
    public ResponseEntity<ApiResponse<PageResponse<CropResponse>>> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) Long parcelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Crop> result = cropUseCase.list(user.id(), parcelId, PageQuery.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result, CropResponse::from)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un cultivo propio")
    public ResponseEntity<ApiResponse<CropResponse>> get(@AuthenticationPrincipal AuthenticatedUser user,
                                                         @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(CropResponse.from(cropUseCase.get(user.id(), id))));
    }

    @PostMapping
    @Operation(summary = "Crear un cultivo")
    public ResponseEntity<ApiResponse<CropResponse>> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CropRequest request) {
        Crop created = idempotencyService.execute(user.id(), idempotencyKey, "CROP",
                id -> Optional.of(cropUseCase.get(user.id(), id)),
                () -> cropUseCase.create(user.id(), request.toCommand()), Crop::id);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(CropResponse.from(created)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un cultivo propio")
    public ResponseEntity<ApiResponse<CropResponse>> update(@AuthenticationPrincipal AuthenticatedUser user,
                                                            @PathVariable Long id,
                                                            @Valid @RequestBody CropRequest request) {
        Crop updated = cropUseCase.update(user.id(), id, request.toCommand());
        return ResponseEntity.ok(ApiResponse.message("Cultivo actualizado correctamente", CropResponse.from(updated)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar el estado de un cultivo")
    public ResponseEntity<ApiResponse<CropResponse>> changeStatus(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody ChangeCropStatusRequest request) {
        Crop updated = cropUseCase.changeStatus(user.id(), id, request.status());
        return ResponseEntity.ok(ApiResponse.message("Estado del cultivo actualizado", CropResponse.from(updated)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un cultivo propio")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable Long id) {
        cropUseCase.delete(user.id(), id);
        return ResponseEntity.ok(ApiResponse.message("Cultivo eliminado correctamente", null));
    }
}
