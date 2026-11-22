package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.model.AuthResult;
import com.agroconecta.application.port.in.AuthUseCase;
import com.agroconecta.infrastructure.adapter.in.web.dto.AuthDtos.AuthResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.AuthDtos.LoginRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.AuthDtos.RegisterRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, inicio y cierre de sesión")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar un productor o comprador")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResult result = authUseCase.register(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(AuthResponse.from(result)));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener un token JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authUseCase.login(request.toCommand());
        return ResponseEntity.ok(ApiResponse.message("Inicio de sesión exitoso", AuthResponse.from(result)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión (el cliente descarta el token JWT)")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.message("Sesión cerrada correctamente", null));
    }
}
