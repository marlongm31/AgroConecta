package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.model.StoredFileContent;
import com.agroconecta.application.port.out.FileStoragePort;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Entrega las fotos guardadas en la base de datos. Solo carpetas públicas
 * (productos y avatares); los adjuntos del chat siguen protegidos por SecurityConfig.
 */
@RestController
public class UploadController {

    private final FileStoragePort fileStorage;

    public UploadController(FileStoragePort fileStorage) {
        this.fileStorage = fileStorage;
    }

    @GetMapping("/uploads/{folder:products|avatars}/{fileName:.+}")
    public ResponseEntity<byte[]> get(@PathVariable String folder, @PathVariable String fileName) {
        StoredFileContent file = fileStorage.load("/uploads/" + folder + "/" + fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(file.content());
    }
}
