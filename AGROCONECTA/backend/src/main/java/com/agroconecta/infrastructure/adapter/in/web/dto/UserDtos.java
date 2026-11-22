package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.application.command.ChangePasswordCommand;
import com.agroconecta.application.command.UpdateProfileCommand;
import com.agroconecta.domain.model.Role;
import com.agroconecta.domain.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class UserDtos {

    private UserDtos() {
    }

    public record UserResponse(
            Long id,
            String fullName,
            String email,
            Role role,
            String phone,
            String location,
            String avatarUrl,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {

        public static UserResponse from(User user) {
            return new UserResponse(user.id(), user.fullName(), user.email(), user.role(), user.phone(),
                    user.location(), user.avatarUrl(), user.active(), user.createdAt(), user.updatedAt());
        }
    }

    public record UpdateProfileRequest(
            @NotBlank(message = "El nombre completo es obligatorio")
            @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
            String fullName,
            @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
            String phone,
            @Size(max = 120, message = "La ubicación no puede superar 120 caracteres")
            String location
    ) {

        public UpdateProfileCommand toCommand() {
            return new UpdateProfileCommand(fullName, phone, location);
        }
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "La contraseña actual es obligatoria")
            String currentPassword,
            @NotBlank(message = "La nueva contraseña es obligatoria")
            @Size(min = 8, max = 72, message = "La nueva contraseña debe tener entre 8 y 72 caracteres")
            String newPassword
    ) {

        public ChangePasswordCommand toCommand() {
            return new ChangePasswordCommand(currentPassword, newPassword);
        }
    }

    public record UpdateStatusRequest(
            @NotNull(message = "El estado es obligatorio")
            Boolean active
    ) {
    }

    public record UpdateRoleRequest(
            @NotNull(message = "El rol es obligatorio")
            Role role
    ) {
    }
}
