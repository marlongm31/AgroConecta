package com.agroconecta.domain.validation;

import com.agroconecta.domain.exception.BusinessRuleException;

import java.util.Locale;

public final class ImageValidator {

    public static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    private ImageValidator() {
    }

    public static String detectExtension(byte[] content) {
        if (content == null || content.length == 0) {
            throw new BusinessRuleException("El archivo de imagen está vacío");
        }
        if (content.length > MAX_IMAGE_BYTES) {
            throw new BusinessRuleException("La imagen supera el tamaño máximo de 5 MB");
        }
        if (isJpeg(content)) {
            return "jpg";
        }
        if (isPng(content)) {
            return "png";
        }
        if (isWebp(content)) {
            return "webp";
        }
        throw new BusinessRuleException("Formato de imagen no permitido. Usa JPG, PNG o WEBP");
    }

    public static String contentTypeFor(String extension) {
        return switch (extension.toLowerCase(Locale.ROOT)) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> throw new BusinessRuleException("Formato de imagen no permitido");
        };
    }

    private static boolean isJpeg(byte[] content) {
        return content.length >= 3
                && (content[0] & 0xFF) == 0xFF
                && (content[1] & 0xFF) == 0xD8
                && (content[2] & 0xFF) == 0xFF;
    }

    private static boolean isPng(byte[] content) {
        byte[] signature = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (content.length < signature.length) {
            return false;
        }
        for (int index = 0; index < signature.length; index++) {
            if (content[index] != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWebp(byte[] content) {
        return content.length >= 12
                && content[0] == 'R' && content[1] == 'I' && content[2] == 'F' && content[3] == 'F'
                && content[8] == 'W' && content[9] == 'E' && content[10] == 'B' && content[11] == 'P';
    }
}
