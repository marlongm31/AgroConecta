package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.model.ProductCatalogItem;
import com.agroconecta.application.port.in.ProductImageUseCase;
import com.agroconecta.application.port.in.ProductUseCase;
import com.agroconecta.domain.model.Product;
import com.agroconecta.domain.model.ProductCategory;
import com.agroconecta.infrastructure.adapter.in.security.AuthenticatedUser;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.ApiResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.PageResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.ProductDtos.ChangeProductStatusRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.ProductDtos.ProductRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.ProductDtos.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Catálogo", description = "Publicación y consulta de productos agrícolas")
public class ProductController {

    private final ProductUseCase productUseCase;
    private final ProductImageUseCase productImageUseCase;

    public ProductController(ProductUseCase productUseCase, ProductImageUseCase productImageUseCase) {
        this.productUseCase = productUseCase;
        this.productImageUseCase = productImageUseCase;
    }

    @GetMapping
    @Operation(summary = "Buscar productos activos en el catálogo público")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        PageResult<ProductCatalogItem> result =
                productUseCase.searchCatalog(search, category, PageQuery.of(page, size));
        Map<Long, List<String>> images = imagesFor(result.content());
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.from(result, item -> ProductResponse.from(item, images.get(item.product().id())))));
    }

    @GetMapping("/mine")
    @Operation(summary = "Listar los productos publicados por el productor autenticado")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> listOwn(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Product> result = productUseCase.listOwn(user.id(), PageQuery.of(page, size));
        Map<Long, List<String>> images = productImageUseCase.imageUrlsByProduct(
                result.content().stream().map(Product::id).toList());
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result,
                product -> ProductResponse.from(product, images.get(product.id())))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un producto activo del catálogo")
    public ResponseEntity<ApiResponse<ProductResponse>> get(@PathVariable Long id) {
        ProductCatalogItem item = productUseCase.getCatalogItem(id);
        return ResponseEntity.ok(ApiResponse.ok(
                ProductResponse.from(item, productImageUseCase.imageUrlsFor(List.of(item.product().id())))));
    }

    @PostMapping
    @Operation(summary = "Publicar un producto")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@AuthenticationPrincipal AuthenticatedUser user,
                                                               @Valid @RequestBody ProductRequest request) {
        Product created = productUseCase.create(user.id(), request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ProductResponse.from(created, List.of())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto propio")
    public ResponseEntity<ApiResponse<ProductResponse>> update(@AuthenticationPrincipal AuthenticatedUser user,
                                                               @PathVariable Long id,
                                                               @Valid @RequestBody ProductRequest request) {
        Product updated = productUseCase.update(user.id(), id, request.toCommand());
        return ResponseEntity.ok(ApiResponse.message("Producto actualizado correctamente",
                ProductResponse.from(updated, productImageUseCase.imageUrlsFor(List.of(id)))));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activar o desactivar un producto propio")
    public ResponseEntity<ApiResponse<ProductResponse>> changeStatus(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody ChangeProductStatusRequest request) {
        Product updated = productUseCase.changeStatus(user.id(), id, request.active());
        return ResponseEntity.ok(ApiResponse.message("Estado del producto actualizado",
                ProductResponse.from(updated, productImageUseCase.imageUrlsFor(List.of(id)))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto propio")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable Long id) {
        productUseCase.delete(user.id(), id);
        return ResponseEntity.ok(ApiResponse.message("Producto eliminado correctamente", null));
    }

    private Map<Long, List<String>> imagesFor(List<ProductCatalogItem> items) {
        return productImageUseCase.imageUrlsByProduct(
                items.stream().map(item -> item.product().id()).toList());
    }
}
