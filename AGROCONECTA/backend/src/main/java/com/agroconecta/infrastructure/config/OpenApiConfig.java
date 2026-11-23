package com.agroconecta.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI agroConectaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AgroConecta API")
                        .version("1.0.0")
                        .description("""
                                API REST para la gestión agrícola y el catálogo comercial de pequeños productores.
                                Autenticación mediante JWT. Usa el botón Authorize con el token obtenido en
                                /api/auth/login para probar los endpoints protegidos.
                                """)
                        .contact(new Contact().name("Equipo AgroConecta").email("contacto@agroconecta.co")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
