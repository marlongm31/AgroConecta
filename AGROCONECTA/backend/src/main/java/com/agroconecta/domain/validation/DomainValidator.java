package com.agroconecta.domain.validation;

import com.agroconecta.domain.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class DomainValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private DomainValidator() {
    }

    public static String requiredText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("El campo " + field + " es obligatorio");
        }
        return value.trim();
    }

    public static Long requireId(Long value, String field) {
        if (value == null || value <= 0) {
            throw new BusinessRuleException("El identificador de " + field + " no es válido");
        }
        return value;
    }

    public static String text(String value, int maxLength, String field) {
        String normalized = requiredText(value, field);
        if (normalized.length() > maxLength) {
            throw new BusinessRuleException("El campo " + field + " no puede superar " + maxLength + " caracteres");
        }
        return normalized;
    }

    public static String optionalText(String value) {
        return value == null ? null : value.trim();
    }

    public static String optionalText(String value, int maxLength, String field) {
        String normalized = optionalText(value);
        if (normalized != null && normalized.length() > maxLength) {
            throw new BusinessRuleException("El campo " + field + " no puede superar " + maxLength + " caracteres");
        }
        return normalized;
    }

    public static String email(String value) {
        String email = requiredText(value, "correo electrónico").toLowerCase();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessRuleException("El correo electrónico no tiene un formato válido");
        }
        return email;
    }

    public static BigDecimal positive(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("El campo " + field + " debe ser mayor que cero");
        }
        return value;
    }

    public static BigDecimal nonNegative(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("El campo " + field + " no puede ser negativo");
        }
        return value;
    }

    public static String password(String value) {
        String password = requiredText(value, "contraseña");
        if (password.length() < 8) {
            throw new BusinessRuleException("La contraseña debe tener al menos 8 caracteres");
        }
        if (password.length() > 72) {
            throw new BusinessRuleException("La contraseña no puede superar 72 caracteres");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BusinessRuleException("La contraseña debe combinar letras y números");
        }
        return password;
    }
}
