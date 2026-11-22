package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.model.AuthResult;
import com.agroconecta.application.port.in.AuthUseCase;
import com.agroconecta.domain.exception.ConflictException;
import com.agroconecta.domain.exception.InvalidCredentialsException;
import com.agroconecta.domain.model.Role;
import com.agroconecta.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController: capa web de autenticación")
class AuthControllerTest {

    @Mock
    private AuthUseCase authUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/register devuelve 201 con el token y el usuario")
    void registerReturnsCreated() throws Exception {
        User user = new User(1L, "Ana Ruiz", "ana@agroconecta.co", "hash", Role.PRODUCTOR,
                "300", "Valledupar", null, true, null, null);
        when(authUseCase.register(any())).thenReturn(new AuthResult("jwt-token", user));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Ana Ruiz","email":"ana@agroconecta.co",
                                 "password":"Agroconecta123","role":"PRODUCTOR"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.role").value("PRODUCTOR"));
    }

    @Test
    @DisplayName("POST /api/auth/register devuelve 400 cuando los datos no son válidos")
    void registerRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"","email":"no-es-correo","password":"corta","role":"PRODUCTOR"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    @DisplayName("POST /api/auth/login devuelve 200 con el token")
    void loginReturnsToken() throws Exception {
        User user = new User(2L, "María", "maria@agroconecta.co", "hash", Role.PRODUCTOR,
                null, null, null, true, null, null);
        when(authUseCase.login(any())).thenReturn(new AuthResult("jwt-token", user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"maria@agroconecta.co","password":"Agroconecta123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login devuelve 401 con credenciales inválidas")
    void loginReturnsUnauthorized() throws Exception {
        when(authUseCase.login(any())).thenThrow(new InvalidCredentialsException("Correo o contraseña incorrectos"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"maria@agroconecta.co","password":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/register devuelve 409 cuando el correo ya existe")
    void registerReturnsConflict() throws Exception {
        when(authUseCase.register(any())).thenThrow(new ConflictException("Correo ya registrado"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Ana","email":"ana@agroconecta.co",
                                 "password":"Agroconecta123","role":"COMPRADOR"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }
}
