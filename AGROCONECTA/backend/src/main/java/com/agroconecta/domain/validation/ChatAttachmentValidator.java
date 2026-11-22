package com.agroconecta.domain.validation;

import com.agroconecta.domain.exception.BusinessRuleException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

public final class ChatAttachmentValidator {

    public static final long MAX_ATTACHMENT_BYTES = 5L * 1024 * 1024;

    private ChatAttachmentValidator() {
    }

    public record Result(String extension, String contentType, String name) {
    }

    public static Result validate(String fileName, byte[] content) {
        return validate(fileName, null, content);
    }

    public static Result validate(String fileName, String declaredContentType, byte[] content) {
        if (content == null || content.length == 0) {
            throw new BusinessRuleException("El archivo adjunto está vacío");
        }
        if (content.length > MAX_ATTACHMENT_BYTES) {
            throw new BusinessRuleException("El archivo supera el tamaño máximo de 5 MB");
        }

        String extension = detect(fileName, content);
        String contentType = contentTypeFor(extension, declaredContentType);
        return new Result(extension, contentType, sanitizeName(fileName, extension));
    }

    private static String detect(String fileName, byte[] content) {
        if (isJpeg(content) || isPng(content) || isWebp(content)) {
            String imageExtension = ImageValidator.detectExtension(content);
            requireExtension(fileName, imageExtension.equals("jpg") ? Set.of("jpg", "jpeg") : Set.of(imageExtension));
            return imageExtension;
        }
        if (startsWith(content, "%PDF-".getBytes(StandardCharsets.US_ASCII))) {
            requireExtension(fileName, Set.of("pdf"));
            return "pdf";
        }
        if (isOfficeOpenXml(content)) {
            String extension = containsAscii(content, "word/") ? "docx" : "xlsx";
            requireExtension(fileName, Set.of(extension));
            return extension;
        }
        if (isOleCompound(content)) {
            String extension = fileNameExtension(fileName);
            if (Set.of("doc", "xls").contains(extension)) {
                return extension;
            }
            throw new BusinessRuleException("Solo se permiten documentos PDF, Word o Excel");
        }
        if (isIsoBmff(content)) {
            requireExtension(fileName, Set.of("mp4", "m4a"));
            return fileNameExtension(fileName);
        }
        if (isWebM(content)) {
            requireExtension(fileName, Set.of("webm"));
            return "webm";
        }
        if (startsWith(content, "OggS".getBytes(StandardCharsets.US_ASCII))) {
            requireExtension(fileName, Set.of("ogg"));
            return "ogg";
        }
        if (isWave(content)) {
            requireExtension(fileName, Set.of("wav"));
            return "wav";
        }
        if (isMp3(content)) {
            requireExtension(fileName, Set.of("mp3"));
            return "mp3";
        }
        throw new BusinessRuleException(
                "Formato de archivo no permitido. Usa JPG, PNG, WEBP, PDF, Word, Excel, MP4, WebM, WAV, OGG, MP3 o M4A");
    }

    private static void requireExtension(String fileName, Set<String> allowed) {
        if (!allowed.contains(fileNameExtension(fileName))) {
            throw new BusinessRuleException("La extensión del archivo no coincide con su contenido");
        }
    }

    private static String fileNameExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int separator = fileName.lastIndexOf('.');
        if (separator < 0 || separator == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(separator + 1).toLowerCase(Locale.ROOT);
    }

    private static String sanitizeName(String fileName, String extension) {
        String name = fileName == null ? "" : fileName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).trim();
        name = name.replaceAll("[\\r\\n\\t\"<>|?*:]", "_");
        if (name.length() > 120) {
            name = name.substring(0, 120);
        }
        if (name.isBlank() || !name.contains(".")) {
            return "archivo." + extension;
        }
        return name;
    }

    private static String contentTypeFor(String extension, String declaredContentType) {
        String declared = declaredContentType == null ? "" : declaredContentType.toLowerCase(Locale.ROOT).trim();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "mp4" -> declared.startsWith("audio/") ? "audio/mp4" : "video/mp4";
            case "m4a" -> "audio/mp4";
            case "webm" -> declared.startsWith("audio/") ? "audio/webm" : "video/webm";
            case "ogg" -> "audio/ogg";
            case "wav" -> "audio/wav";
            case "mp3" -> "audio/mpeg";
            default -> throw new BusinessRuleException("Formato de archivo no permitido");
        };
    }

    private static boolean isJpeg(byte[] content) {
        return content.length >= 3
                && (content[0] & 0xFF) == 0xFF
                && (content[1] & 0xFF) == 0xD8
                && (content[2] & 0xFF) == 0xFF;
    }

    private static boolean isPng(byte[] content) {
        return startsWith(content, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
    }

    private static boolean isWebp(byte[] content) {
        return content.length >= 12
                && content[0] == 'R' && content[1] == 'I' && content[2] == 'F' && content[3] == 'F'
                && content[8] == 'W' && content[9] == 'E' && content[10] == 'B' && content[11] == 'P';
    }

    private static boolean isIsoBmff(byte[] content) {
        return content.length >= 12 && content[4] == 'f' && content[5] == 't' && content[6] == 'y' && content[7] == 'p';
    }

    private static boolean isWebM(byte[] content) {
        return isEbml(content) && containsAscii(content, "webm");
    }

    private static boolean isEbml(byte[] content) {
        return startsWith(content, new byte[]{0x1A, 0x45, (byte) 0xDF, (byte) 0xA3});
    }

    private static boolean isWave(byte[] content) {
        return content.length >= 12
                && content[0] == 'R' && content[1] == 'I' && content[2] == 'F' && content[3] == 'F'
                && content[8] == 'W' && content[9] == 'A' && content[10] == 'V' && content[11] == 'E';
    }

    private static boolean isMp3(byte[] content) {
        if (startsWith(content, "ID3".getBytes(StandardCharsets.US_ASCII))) {
            return true;
        }
        return content.length >= 2 && (content[0] & 0xFF) == 0xFF && (content[1] & 0xE0) == 0xE0;
    }

    private static boolean isOfficeOpenXml(byte[] content) {
        if (!startsWith(content, new byte[]{0x50, 0x4B, 0x03, 0x04})) {
            return false;
        }
        return containsAscii(content, "[Content_Types].xml")
                && (containsAscii(content, "word/") || containsAscii(content, "xl/"));
    }

    private static boolean isOleCompound(byte[] content) {
        return startsWith(content, new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
                (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1});
    }

    private static boolean startsWith(byte[] content, byte[] signature) {
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

    private static boolean containsAscii(byte[] content, String value) {
        int limit = Math.min(content.length, 8192);
        String head = new String(content, 0, limit, StandardCharsets.ISO_8859_1);
        return head.contains(value);
    }
}
