package com.agroconecta.infrastructure.adapter.out.storage;

import com.agroconecta.application.model.StoredFile;
import com.agroconecta.application.model.StoredFileContent;
import com.agroconecta.application.port.out.FileStoragePort;
import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.validation.ImageValidator;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Guarda los archivos en la tabla stored_files de PostgreSQL en lugar del disco local.
 * Es necesario en hosting gratuito (Render), donde el disco se borra al reiniciar.
 * Mantiene las mismas URL públicas (/uploads/{carpeta}/{archivo}) que el adaptador local.
 */
@Component
@Primary
public class DatabaseFileStorageAdapter implements FileStoragePort {

    private final JdbcTemplate jdbc;

    public DatabaseFileStorageAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public StoredFile storeImage(String folder, byte[] content, String extension) {
        return storeFile(folder, content, extension, ImageValidator.contentTypeFor(extension));
    }

    @Override
    public StoredFile storeFile(String folder, byte[] content, String extension, String contentType) {
        if (folder == null || !folder.matches("[a-z0-9-]{1,30}")) {
            throw new BusinessRuleException("Carpeta de almacenamiento no válida");
        }
        String safeExtension = extension.toLowerCase(Locale.ROOT);
        if (!safeExtension.matches("[a-z0-9]{1,10}")) {
            throw new BusinessRuleException("Extensión de archivo no válida");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + safeExtension;
        String url = "/uploads/" + folder + "/" + fileName;
        jdbc.update(
                "INSERT INTO stored_files (public_url, content_type, size_bytes, content) VALUES (?, ?, ?, ?)",
                url, contentType, (long) content.length, content);
        return new StoredFile(url, contentType, content.length);
    }

    @Override
    public StoredFileContent load(String publicUrl) {
        String[] parts = validate(publicUrl);
        String fileName = parts[1];
        List<StoredFileContent> rows = jdbc.query(
                "SELECT content, content_type FROM stored_files WHERE public_url = ?",
                (rs, rowNum) -> new StoredFileContent(rs.getBytes("content"), rs.getString("content_type"), fileName),
                publicUrl);
        if (rows.isEmpty()) {
            throw new NotFoundException("Archivo adjunto");
        }
        return rows.get(0);
    }

    @Override
    public void delete(String publicUrl) {
        if (publicUrl == null || !publicUrl.startsWith("/uploads/")) {
            return;
        }
        jdbc.update("DELETE FROM stored_files WHERE public_url = ?", publicUrl);
    }

    private String[] validate(String publicUrl) {
        if (publicUrl == null || !publicUrl.startsWith("/uploads/")) {
            throw new BusinessRuleException("Ruta de archivo no válida");
        }
        String[] parts = publicUrl.substring("/uploads/".length()).split("/");
        if (parts.length != 2 || !parts[0].matches("[a-z0-9-]{1,30}")
                || !parts[1].matches("[a-zA-Z0-9._-]{1,120}") || parts[1].contains("..")) {
            throw new BusinessRuleException("Ruta de archivo no válida");
        }
        return parts;
    }
}
