package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.in.ActivityUseCase;
import com.agroconecta.application.usecase.IdempotencyService;
import com.agroconecta.domain.model.AgriculturalActivity;
import com.agroconecta.infrastructure.adapter.in.security.AuthenticatedUser;
import com.agroconecta.infrastructure.adapter.in.web.dto.ActivityDtos.ActivityRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.ActivityDtos.ActivityResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.ApiResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.PageResponse;
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
@RequestMapping("/api/activities")
@Tag(name = "Actividades agrícolas", description = "Registro de siembra, riego, fertilización, poda y cosecha")
public class ActivityController {

    private final ActivityUseCase activityUseCase;
    private final IdempotencyService idempotencyService;

    public ActivityController(ActivityUseCase activityUseCase, IdempotencyService idempotencyService) {
        this.activityUseCase = activityUseCase;
        this.idempotencyService = idempotencyService;
    }

    @GetMapping
    @Operation(summary = "Listar actividades del productor, opcionalmente por cultivo")
    public ResponseEntity<ApiResponse<PageResponse<ActivityResponse>>> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) Long cropId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<AgriculturalActivity> result = activityUseCase.list(user.id(), cropId, PageQuery.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result, ActivityResponse::from)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una actividad propia")
    public ResponseEntity<ApiResponse<ActivityResponse>> get(@AuthenticationPrincipal AuthenticatedUser user,
                                                             @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ActivityResponse.from(activityUseCase.get(user.id(), id))));
    }

    @PostMapping
    @Operation(summary = "Registrar una actividad agrícola")
    public ResponseEntity<ApiResponse<ActivityResponse>> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ActivityRequest request) {
        AgriculturalActivity created = idempotencyService.execute(user.id(), idempotencyKey, "ACTIVITY",
                id -> Optional.of(activityUseCase.get(user.id(), id)),
                () -> activityUseCase.create(user.id(), request.toCommand()), AgriculturalActivity::id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ActivityResponse.from(created)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una actividad propia")
    public ResponseEntity<ApiResponse<ActivityResponse>> update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody ActivityRequest request) {
        AgriculturalActivity updated = activityUseCase.update(user.id(), id, request.toCommand());
        return ResponseEntity.ok(ApiResponse.message("Actividad actualizada correctamente",
                ActivityResponse.from(updated)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una actividad propia")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable Long id) {
        activityUseCase.delete(user.id(), id);
        return ResponseEntity.ok(ApiResponse.message("Actividad eliminada correctamente", null));
    }
}
