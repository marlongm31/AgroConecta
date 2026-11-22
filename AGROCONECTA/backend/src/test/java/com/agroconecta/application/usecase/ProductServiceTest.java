package com.agroconecta.application.usecase;

import com.agroconecta.application.command.ProductCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.model.ProductCatalogItem;
import com.agroconecta.application.port.out.ProductRepositoryPort;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.model.MeasurementUnit;
import com.agroconecta.domain.model.Product;
import com.agroconecta.domain.model.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService: catálogo y publicación de productos")
class ProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    private ProductCommand command() {
        return command(new BigDecimal("120"));
    }

    private ProductCommand command(BigDecimal availableQuantity) {
        return new ProductCommand("Tomate chonto", "Fresco", null, ProductCategory.HORTALIZAS,
                new BigDecimal("3500"), availableQuantity, MeasurementUnit.KILOGRAMO,
                "3001112233", "maria@agroconecta.co", true);
    }

    @Test
    @DisplayName("Normaliza una búsqueda en blanco a null")
    void normalizesBlankSearch() {
        PageResult<ProductCatalogItem> empty = new PageResult<>(List.of(), 0, 12, 0, 0);
        when(productRepository.searchActive(isNull(), isNull(), eq(PageQuery.of(0, 12)))).thenReturn(empty);

        PageResult<ProductCatalogItem> result = productService.searchCatalog("   ", null, PageQuery.of(0, 12));

        assertThat(result.content()).isEmpty();
        verify(productRepository).searchActive(isNull(), isNull(), eq(PageQuery.of(0, 12)));
    }

    @Test
    @DisplayName("Recorta y conserva el texto de búsqueda")
    void trimsSearch() {
        PageResult<ProductCatalogItem> empty = new PageResult<>(List.of(), 0, 12, 0, 0);
        when(productRepository.searchActive(eq("café"), isNull(), any(PageQuery.class))).thenReturn(empty);

        productService.searchCatalog("  café  ", null, PageQuery.of(0, 12));

        verify(productRepository).searchActive(eq("café"), isNull(), any(PageQuery.class));
    }

    @Test
    @DisplayName("Lanza NotFound cuando el producto del catálogo no existe o está inactivo")
    void catalogItemNotFound() {
        when(productRepository.findActiveById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getCatalogItem(7L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Publica un producto a nombre del productor autenticado")
    void publishesProduct() {
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product product = productService.create(2L, command());

        assertThat(product.ownerId()).isEqualTo(2L);
        assertThat(product.active()).isTrue();
        assertThat(product.price()).isEqualByComparingTo("3500");
    }

    @Test
    @DisplayName("Impide editar productos de otro productor")
    void forbidsEditingForeignProduct() {
        Product product = new Product(1L, 5L, "Ajeno", "Desc", null, ProductCategory.CAFE, BigDecimal.ONE,
                BigDecimal.ONE, MeasurementUnit.KILOGRAMO, "300", "a@b.co", true, false, null, null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.update(2L, 1L, command()))
                .isInstanceOf(ForbiddenException.class);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Desactiva un producto propio")
    void deactivatesOwnProduct() {
        Product product = new Product(1L, 2L, "Tomate", "Desc", null, ProductCategory.HORTALIZAS, BigDecimal.ONE,
                BigDecimal.ONE, MeasurementUnit.KILOGRAMO, "300", "a@b.co", true, false, null, null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.changeStatus(2L, 1L, false);

        assertThat(result.active()).isFalse();
    }

    @Test
    @DisplayName("Marca como agotado un producto publicado sin existencias")
    void createsSoldOutProduct() {
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product product = productService.create(2L, command(BigDecimal.ZERO));

        assertThat(product.active()).isTrue();
        assertThat(product.soldOut()).isTrue();
    }

    @Test
    @DisplayName("Repone la disponibilidad al actualizar la cantidad")
    void restoresAvailabilityWhenQuantityIsAdded() {
        Product product = new Product(1L, 2L, "Tomate", "Desc", null, ProductCategory.HORTALIZAS, BigDecimal.ONE,
                BigDecimal.ZERO, MeasurementUnit.KILOGRAMO, "300", "a@b.co", true, true, null, null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.update(2L, 1L, command(new BigDecimal("50")));

        assertThat(result.active()).isTrue();
        assertThat(result.soldOut()).isFalse();
    }

    @Test
    @DisplayName("Reactivar sin existencias mantiene el producto agotado")
    void reactivatesProductAsSoldOut() {
        Product product = new Product(1L, 2L, "Tomate", "Desc", null, ProductCategory.HORTALIZAS, BigDecimal.ONE,
                BigDecimal.ZERO, MeasurementUnit.KILOGRAMO, "300", "a@b.co", false, true, null, null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.changeStatus(2L, 1L, true);

        assertThat(result.active()).isTrue();
        assertThat(result.soldOut()).isTrue();
    }
}
