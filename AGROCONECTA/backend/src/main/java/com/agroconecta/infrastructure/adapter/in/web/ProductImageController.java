package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.command.ImageUploadCommand;
import com.agroconecta.application.port.in.ProductImageUseCase;
import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.model.ProductImage;
import com.agroconecta.infrastructure.adapter.in.security.AuthenticatedUser;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.ApiResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.ProductImageDtos.ProductImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Fotografías de productos", description = "Carga y administración de fotografías de las cosechas")
public class ProductImageController {

    private final ProductImageUseCase productImageUseCase;

    public ProductImageController(ProductImageUseCase productImageUseCase) {
        this.productImageUseCase = productImageUseCase;
    }

    @GetMapping("/{productId}/images")
    @Operation(summary = "Listar las fotografías de un producto propio")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long productId) {
        List<ProductImageResponse> images = productImageUseCase.listForProduct(user.id(), productId).stream()
                .map(ProductImageResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(images));
    }

    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir una fotografía de un producto propio")
    public ResponseEntity<ApiResponse<ProductImageResponse>> upload(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Debes seleccionar una imagen");
        }
        try {
            ProductImage image = productImageUseCase.upload(user.id(), productId,
                    new ImageUploadCommand(file.getOriginalFilename(), file.getContentType(), file.getBytes()));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(ProductImageResponse.from(image)));
        } catch (IOException exception) {
            throw new BusinessRuleException("No se pudo leer el archivo de imagen");
        }
    }

    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "Eliminar una fotografía de un producto propio")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable Long imageId) {
        productImageUseCase.delete(user.id(), imageId);
        return ResponseEntity.ok(ApiResponse.message("Fotografía eliminada correctamente", null));
    }
}
