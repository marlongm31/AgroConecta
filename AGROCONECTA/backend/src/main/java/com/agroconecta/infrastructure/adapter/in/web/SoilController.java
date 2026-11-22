package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.in.SoilUseCase;
import com.agroconecta.domain.model.SoilAssessment;
import com.agroconecta.infrastructure.adapter.in.security.AuthenticatedUser;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.ApiResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.PageResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.SoilDtos.SoilAssessmentRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.SoilDtos.SoilAssessmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/soil/assessments")
@Tag(name = "Suelo", description = "Cuestionario y orientación básica sobre el suelo de las parcelas")
public class SoilController {

    private final SoilUseCase soilUseCase;

    public SoilController(SoilUseCase soilUseCase) {
        this.soilUseCase = soilUseCase;
    }

    @PostMapping
    @Operation(summary = "Registrar un cuestionario de suelo y obtener una orientación")
    public ResponseEntity<ApiResponse<SoilAssessmentResponse>> assess(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody SoilAssessmentRequest request) {
        SoilAssessment assessment = soilUseCase.assess(user.id(), request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(SoilAssessmentResponse.from(assessment)));
    }

    @GetMapping
    @Operation(summary = "Listar los análisis de suelo del productor autenticado")
    public ResponseEntity<ApiResponse<PageResponse<SoilAssessmentResponse>>> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<SoilAssessment> result = soilUseCase.list(user.id(), PageQuery.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result, SoilAssessmentResponse::from)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un análisis de suelo propio")
    public ResponseEntity<ApiResponse<SoilAssessmentResponse>> get(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(SoilAssessmentResponse.from(soilUseCase.get(user.id(), id))));
    }
}
