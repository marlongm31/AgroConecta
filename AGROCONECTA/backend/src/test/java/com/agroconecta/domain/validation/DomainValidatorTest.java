package com.agroconecta.domain.validation;

import com.agroconecta.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DomainValidator: reglas de validación del dominio")
class DomainValidatorTest {

    @Test
    @DisplayName("requiredText recorta espacios y rechaza textos vacíos")
    void requiredText() {
        assertThat(DomainValidator.requiredText("  María  ", "nombre")).isEqualTo("María");
        assertThatThrownBy(() -> DomainValidator.requiredText("   ", "nombre"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("nombre");
        assertThatThrownBy(() -> DomainValidator.requiredText(null, "nombre"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("email normaliza a minúsculas y valida el formato")
    void email() {
        assertThat(DomainValidator.email("  Usuario@AgroConecta.CO ")).isEqualTo("usuario@agroconecta.co");
        assertThatThrownBy(() -> DomainValidator.email("correo-invalido"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("formato válido");
    }

    @Test
    @DisplayName("positive exige valores mayores que cero")
    void positive() {
        assertThat(DomainValidator.positive(new BigDecimal("1.50"), "área"))
                .isEqualByComparingTo("1.50");
        assertThatThrownBy(() -> DomainValidator.positive(BigDecimal.ZERO, "área"))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> DomainValidator.positive(null, "área"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("nonNegative permite cero pero rechaza negativos")
    void nonNegative() {
        assertThat(DomainValidator.nonNegative(BigDecimal.ZERO, "cantidad"))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThatThrownBy(() -> DomainValidator.nonNegative(new BigDecimal("-1"), "cantidad"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("password exige longitud mínima y combinación de letras y números")
    void password() {
        assertThat(DomainValidator.password("Agroconecta123")).isEqualTo("Agroconecta123");
        assertThatThrownBy(() -> DomainValidator.password("corta1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("al menos 8 caracteres");
        assertThatThrownBy(() -> DomainValidator.password("solamenteletras"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("letras y números");
        assertThatThrownBy(() -> DomainValidator.password("12345678"))
                .isInstanceOf(BusinessRuleException.class);
    }
}
