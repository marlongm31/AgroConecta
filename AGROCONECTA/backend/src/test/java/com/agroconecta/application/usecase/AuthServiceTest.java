package com.agroconecta.application.usecase;

import com.agroconecta.application.command.LoginCommand;
import com.agroconecta.application.command.RegisterCommand;
import com.agroconecta.application.model.AuthResult;
import com.agroconecta.application.port.out.PasswordHasherPort;
import com.agroconecta.application.port.out.TokenProviderPort;
import com.agroconecta.application.port.out.UserRepositoryPort;
import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.exception.ConflictException;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.InvalidCredentialsException;
import com.agroconecta.domain.model.Role;
import com.agroconecta.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService: registro e inicio de sesión")
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private PasswordHasherPort passwordHasher;
    @Mock
    private TokenProviderPort tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordHasher, tokenProvider);
    }

    @Test
    @DisplayName("Registra un PRODUCTOR, cifra la contraseña y genera token")
    void registersProducerSuccessfully() {
        RegisterCommand command = new RegisterCommand("Ana Ruiz", "ana@agroconecta.co", "Agroconecta123",
                Role.PRODUCTOR, "300", "Valledupar");
        when(userRepository.existsByEmail("ana@agroconecta.co")).thenReturn(false);
        when(passwordHasher.hash("Agroconecta123")).thenReturn("hash-bcrypt");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenProvider.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResult result = authService.register(command);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.user().role()).isEqualTo(Role.PRODUCTOR);
        assertThat(result.user().passwordHash()).isEqualTo("hash-bcrypt");
        verify(passwordHasher).hash("Agroconecta123");
    }

    @Test
    @DisplayName("Impide autoasignarse el rol ADMINISTRADOR durante el registro")
    void rejectsPrivilegedRole() {
        RegisterCommand command = new RegisterCommand("Intruso", "hacker@agroconecta.co", "Agroconecta123",
                Role.ADMINISTRADOR, null, null);

        assertThatThrownBy(() -> authService.register(command))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PRODUCTOR o COMPRADOR");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Rechaza el registro cuando el correo ya existe")
    void rejectsDuplicateEmail() {
        RegisterCommand command = new RegisterCommand("Ana", "ana@agroconecta.co", "Agroconecta123",
                Role.COMPRADOR, null, null);
        when(userRepository.existsByEmail("ana@agroconecta.co")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(command))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Inicia sesión con credenciales válidas")
    void logsInSuccessfully() {
        User user = User.create("Ana", "ana@agroconecta.co", "hash-bcrypt", Role.PRODUCTOR, null, null);
        when(userRepository.findByEmail("ana@agroconecta.co")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("Agroconecta123", "hash-bcrypt")).thenReturn(true);
        when(tokenProvider.generateToken(user)).thenReturn("jwt-token");

        AuthResult result = authService.login(new LoginCommand("ANA@agroconecta.co", "Agroconecta123"));

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.user().email()).isEqualTo("ana@agroconecta.co");
    }

    @Test
    @DisplayName("Rechaza el inicio de sesión cuando la contraseña no coincide")
    void rejectsWrongPassword() {
        User user = User.create("Ana", "ana@agroconecta.co", "hash-bcrypt", Role.PRODUCTOR, null, null);
        when(userRepository.findByEmail("ana@agroconecta.co")).thenReturn(Optional.of(user));
        when(passwordHasher.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginCommand("ana@agroconecta.co", "incorrecta")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Rechaza el acceso de una cuenta inactiva")
    void rejectsInactiveAccount() {
        User user = User.create("Ana", "ana@agroconecta.co", "hash-bcrypt", Role.PRODUCTOR, null, null)
                .withStatus(false);
        when(userRepository.findByEmail("ana@agroconecta.co")).thenReturn(Optional.of(user));
        when(passwordHasher.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginCommand("ana@agroconecta.co", "Agroconecta123")))
                .isInstanceOf(ForbiddenException.class);
    }
}
