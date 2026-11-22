package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.application.command.LoginCommand;
import com.agroconecta.application.command.RegisterCommand;
import com.agroconecta.application.model.AuthResult;
import com.agroconecta.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "El nombre completo es obligatorio")
            @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
            String fullName,
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido")
            @Size(max = 160, message = "El correo no puede superar 160 caracteres")
            String email,
            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
            String password,
            @NotNull(message = "El rol es obligatorio")
            Role role,
            @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
            String phone,
            @Size(max = 120, message = "La ubicación no puede superar 120 caracteres")
            String location
    ) {

        public RegisterCommand toCommand() {
            return new RegisterCommand(fullName, email, password, role, phone, location);
        }
    }

    public record LoginRequest(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido")
            String email,
            @NotBlank(message = "La contraseña es obligatoria")
            String password
    ) {

        public LoginCommand toCommand() {
            return new LoginCommand(email, password);
        }
    }

    public record AuthResponse(String token, UserDtos.UserResponse user) {

        public static AuthResponse from(AuthResult result) {
            return new AuthResponse(result.token(), UserDtos.UserResponse.from(result.user()));
        }
    }
}
