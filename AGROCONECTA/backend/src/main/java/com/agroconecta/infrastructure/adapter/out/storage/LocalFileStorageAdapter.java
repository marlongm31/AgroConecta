package com.agroconecta.infrastructure.adapter.out.storage;

import com.agroconecta.application.model.StoredFile;
import com.agroconecta.application.model.StoredFileContent;
import com.agroconecta.application.port.out.FileStoragePort;
import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.validation.ImageValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private final Path uploadRoot;

    public LocalFileStorageAdapter(@Value("${app.storage.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo crear el directorio de archivos: " + uploadRoot, exception);
        }
    }

    @Override
    public StoredFile storeImage(String folder, byte[] content, String extension) {
        return storeFile(folder, content, extension, ImageValidator.contentTypeFor(extension));
    }

    @Override
    public StoredFile storeFile(String folder, byte[] content, String extension, String contentType) {
        String safeFolder = sanitizeFolder(folder);
        String safeExtension = extension.toLowerCase(Locale.ROOT);
        if (!safeExtension.matches("[a-z0-9]{1,10}")) {
            throw new BusinessRuleException("Extensión de archivo no válida");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + safeExtension;
        Path targetDir = uploadRoot.resolve(safeFolder).normalize();
        if (!targetDir.startsWith(uploadRoot)) {
            throw new BusinessRuleException("Ruta de almacenamiento no válida");
        }
        try {
            Files.createDirectories(targetDir);
            Files.write(targetDir.resolve(fileName), content);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo guardar el archivo", exception);
        }
        String url = "/uploads/" + safeFolder + "/" + fileName;
        return new StoredFile(url, contentType, content.length);
    }

    @Override
    public StoredFileContent load(String publicUrl) {
        if (publicUrl == null || !publicUrl.startsWith("/uploads/")) {
            throw new BusinessRuleException("Ruta de archivo no válida");
        }
        String[] parts = publicUrl.substring("/uploads/".length()).split("/");
        if (parts.length != 2 || !parts[0].matches("[a-z0-9-]{1,30}")
                || !parts[1].matches("[a-zA-Z0-9._-]{1,120}") || parts[1].contains("..")) {
            throw new BusinessRuleException("Ruta de archivo no válida");
        }
        Path target = uploadRoot.resolve(parts[0]).resolve(parts[1]).normalize();
        if (!target.startsWith(uploadRoot) || !Files.isRegularFile(target)) {
            throw new NotFoundException("Archivo adjunto");
        }
        try {
            return new StoredFileContent(Files.readAllBytes(target), contentTypeFor(parts[1]), parts[1]);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo leer el archivo", exception);
        }
    }

    @Override
    public void delete(String publicUrl) {
        if (publicUrl == null || !publicUrl.startsWith("/uploads/")) {
            return;
        }
        Path target = uploadRoot.resolve(publicUrl.substring("/uploads/".length())).normalize();
        if (!target.startsWith(uploadRoot)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            return;
        }
    }

    private String sanitizeFolder(String folder) {
        if (folder == null || !folder.matches("[a-z0-9-]{1,30}")) {
            throw new BusinessRuleException("Carpeta de almacenamiento no válida");
        }
        return folder;
    }

    private String contentTypeFor(String fileName) {
        String extension = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "mp4" -> "video/mp4";
            case "m4a" -> "audio/mp4";
            case "webm" -> "video/webm";
            case "ogg" -> "audio/ogg";
            case "wav" -> "audio/wav";
            case "mp3" -> "audio/mpeg";
            default -> "application/octet-stream";
        };
    }
}
