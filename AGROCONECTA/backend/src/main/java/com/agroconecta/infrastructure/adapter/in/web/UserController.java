package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.command.ImageUploadCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.in.UserUseCase;
import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.model.User;
import com.agroconecta.infrastructure.adapter.in.security.AuthenticatedUser;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.ApiResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.PageResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.UserDtos.ChangePasswordRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.UserDtos.UpdateProfileRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.UserDtos.UpdateRoleRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.UserDtos.UpdateStatusRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.UserDtos.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Perfil y administración de usuarios")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar el perfil del usuario autenticado")
    public ResponseEntity<ApiResponse<UserResponse>> profile(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.from(userUseCase.getProfile(user.id()))));
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar el perfil del usuario autenticado")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UpdateProfileRequest request) {
        User updated = userUseCase.updateProfile(user.id(), request.toCommand());
        return ResponseEntity.ok(ApiResponse.message("Perfil actualizado correctamente", UserResponse.from(updated)));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar la fotografía de perfil del usuario autenticado")
    public ResponseEntity<ApiResponse<UserResponse>> updateAvatar(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Debes seleccionar una imagen");
        }
        try {
            User updated = userUseCase.updateAvatar(user.id(),
                    new ImageUploadCommand(file.getOriginalFilename(), file.getContentType(), file.getBytes()));
            return ResponseEntity.ok(ApiResponse.message("Fotografía de perfil actualizada",
                    UserResponse.from(updated)));
        } catch (IOException exception) {
            throw new BusinessRuleException("No se pudo leer el archivo de imagen");
        }
    }

    @PatchMapping("/me/password")
    @Operation(summary = "Cambiar la contraseña del usuario autenticado")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ChangePasswordRequest request) {
        userUseCase.changePassword(user.id(), request.toCommand());
        return ResponseEntity.ok(ApiResponse.message("Contraseña actualizada correctamente", null));
    }

    @GetMapping
    @Operation(summary = "Listar usuarios (solo administrador)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<User> result = userUseCase.listUsers(user.id(), PageQuery.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result, UserResponse::from)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activar o desactivar un usuario (solo administrador)")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        User updated = userUseCase.updateStatus(user.id(), id, request.active());
        return ResponseEntity.ok(ApiResponse.message("Estado del usuario actualizado", UserResponse.from(updated)));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Cambiar el rol de un usuario (solo administrador)")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        User updated = userUseCase.updateRole(user.id(), id, request.role());
        return ResponseEntity.ok(ApiResponse.message("Rol del usuario actualizado", UserResponse.from(updated)));
    }
}
