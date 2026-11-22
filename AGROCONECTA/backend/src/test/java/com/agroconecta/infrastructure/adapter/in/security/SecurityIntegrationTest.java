package com.agroconecta.infrastructure.adapter.in.security;

import com.agroconecta.application.port.out.UserRepositoryPort;
import com.agroconecta.domain.model.Role;
import com.agroconecta.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Seguridad: autenticación y autorización por roles en la API")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepositoryPort userRepository;

    private String tokenFor(String email, Role role) {
        User user = userRepository.save(User.create("Usuario Prueba", email, "hash", role, null, null));
        return "Bearer " + jwtService.generateToken(user);
    }

    @Test
    @DisplayName("El catálogo es público")
    void catalogIsPublic() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Un endpoint protegido sin token devuelve 401")
    void protectedEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Un COMPRADOR no puede acceder a recursos de productor (403)")
    void buyerCannotAccessProducerResources() throws Exception {
        String token = tokenFor("comprador-seguridad@agroconecta.co", Role.COMPRADOR);

        mockMvc.perform(get("/api/parcels").header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Un PRODUCTOR autenticado accede a sus parcelas")
    void producerCanAccessOwnResources() throws Exception {
        String token = tokenFor("productor-seguridad@agroconecta.co", Role.PRODUCTOR);

        mockMvc.perform(get("/api/parcels").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Un usuario autenticado consulta su propio perfil")
    void authenticatedUserReadsOwnProfile() throws Exception {
        String token = tokenFor("perfil-seguridad@agroconecta.co", Role.PRODUCTOR);

        mockMvc.perform(get("/api/users/me").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("perfil-seguridad@agroconecta.co"));
    }
}
