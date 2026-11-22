package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.in.ParcelUseCase;
import com.agroconecta.domain.model.Parcel;
import com.agroconecta.infrastructure.adapter.in.security.AuthenticatedUser;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.ApiResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.PageResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.ParcelDtos.ParcelRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.ParcelDtos.ParcelResponse;
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
@RequestMapping("/api/parcels")
@Tag(name = "Parcelas", description = "Gestión de parcelas del productor")
public class ParcelController {

    private final ParcelUseCase parcelUseCase;

    public ParcelController(ParcelUseCase parcelUseCase) {
        this.parcelUseCase = parcelUseCase;
    }

    @GetMapping
    @Operation(summary = "Listar las parcelas del productor autenticado")
    public ResponseEntity<ApiResponse<PageResponse<ParcelResponse>>> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Parcel> result = parcelUseCase.list(user.id(), PageQuery.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result, ParcelResponse::from)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una parcela propia")
    public ResponseEntity<ApiResponse<ParcelResponse>> get(@AuthenticationPrincipal AuthenticatedUser user,
                                                           @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ParcelResponse.from(parcelUseCase.get(user.id(), id))));
    }

    @PostMapping
    @Operation(summary = "Crear una parcela")
    public ResponseEntity<ApiResponse<ParcelResponse>> create(@AuthenticationPrincipal AuthenticatedUser user,
                                                              @Valid @RequestBody ParcelRequest request) {
        Parcel created = parcelUseCase.create(user.id(), request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ParcelResponse.from(created)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una parcela propia")
    public ResponseEntity<ApiResponse<ParcelResponse>> update(@AuthenticationPrincipal AuthenticatedUser user,
                                                              @PathVariable Long id,
                                                              @Valid @RequestBody ParcelRequest request) {
        Parcel updated = parcelUseCase.update(user.id(), id, request.toCommand());
        return ResponseEntity.ok(ApiResponse.message("Parcela actualizada correctamente", ParcelResponse.from(updated)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una parcela propia")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable Long id) {
        parcelUseCase.delete(user.id(), id);
        return ResponseEntity.ok(ApiResponse.message("Parcela eliminada correctamente", null));
    }
}
