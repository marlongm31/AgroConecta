package com.agroconecta.infrastructure.adapter.out.persistence;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.model.ProductCatalogItem;
import com.agroconecta.domain.model.MeasurementUnit;
import com.agroconecta.domain.model.Product;
import com.agroconecta.domain.model.ProductCategory;
import com.agroconecta.domain.model.Role;
import com.agroconecta.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({UserPersistenceAdapter.class, ProductPersistenceAdapter.class})
@DisplayName("Persistencia: adaptadores JPA y consulta de catálogo")
class PersistenceAdapterTest {

    @Autowired
    private UserPersistenceAdapter userAdapter;

    @Autowired
    private ProductPersistenceAdapter productAdapter;

    @Test
    @DisplayName("Guarda y recupera un usuario por correo sin distinguir mayúsculas")
    void savesAndFindsUserByEmail() {
        User user = User.create("María Gómez", "maria@agroconecta.co", "hash", Role.PRODUCTOR,
                "+57 312", "Valledupar");

        User saved = userAdapter.save(user);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.createdAt()).isNotNull();
        Optional<User> found = userAdapter.findByEmail("MARIA@AGROCONECTA.CO");
        assertThat(found).isPresent();
        assertThat(found.get().fullName()).isEqualTo("María Gómez");
        assertThat(userAdapter.existsByEmail("maria@agroconecta.co")).isTrue();
    }

    @Test
    @DisplayName("La búsqueda del catálogo funciona por nombre y descripción (regresión lower/concat)")
    void searchesCatalogByText() {
        User producer = userAdapter.save(User.create("José Martínez", "jose@agroconecta.co", "hash",
                Role.PRODUCTOR, "+57 318", "Santa Marta"));

        productAdapter.save(Product.create(producer.id(), "Café Castillo", "Café de altura secado al sol",
                ProductCategory.CAFE, new BigDecimal("18500"), new BigDecimal("300"),
                MeasurementUnit.KILOGRAMO, "318", "jose@agroconecta.co"));
        productAdapter.save(Product.create(producer.id(), "Tomate chonto", "Tomate fresco del valle",
                ProductCategory.HORTALIZAS, new BigDecimal("3500"), new BigDecimal("120"),
                MeasurementUnit.KILOGRAMO, "318", "jose@agroconecta.co"));

        PageResult<ProductCatalogItem> byName = productAdapter.searchActive("CAF", null, PageQuery.of(0, 12));
        PageResult<ProductCatalogItem> byDescription = productAdapter.searchActive("valle", null, PageQuery.of(0, 12));
        PageResult<ProductCatalogItem> byCategory = productAdapter.searchActive(null, ProductCategory.CAFE,
                PageQuery.of(0, 12));

        assertThat(byName.content()).hasSize(1);
        assertThat(byName.content().get(0).product().name()).isEqualTo("Café Castillo");
        assertThat(byName.content().get(0).producerName()).isEqualTo("José Martínez");
        assertThat(byDescription.content()).hasSize(1);
        assertThat(byCategory.content()).hasSize(1);
    }

    @Test
    @DisplayName("La búsqueda del catálogo ignora productos desactivados")
    void searchIgnoresInactiveProducts() {
        User producer = userAdapter.save(User.create("Ana", "ana@agroconecta.co", "hash",
                Role.PRODUCTOR, null, null));
        Product inactive = Product.create(producer.id(), "Maíz amarillo", "Grano seco",
                ProductCategory.GRANOS, new BigDecimal("2400"), BigDecimal.ZERO,
                MeasurementUnit.KILOGRAMO, "300", "ana@agroconecta.co").withStatus(false);
        productAdapter.save(inactive);

        PageResult<ProductCatalogItem> result = productAdapter.searchActive("maíz", null, PageQuery.of(0, 12));

        assertThat(result.content()).isEmpty();
    }

    @Test
    @DisplayName("La búsqueda del catálogo ignora productos agotados")
    void searchIgnoresSoldOutProducts() {
        User producer = userAdapter.save(User.create("Luis", "luis@agroconecta.co", "hash",
                Role.PRODUCTOR, null, null));
        productAdapter.save(Product.create(producer.id(), "Producto agotado", "Sin existencias",
                ProductCategory.GRANOS, new BigDecimal("1000"), BigDecimal.ZERO,
                MeasurementUnit.KILOGRAMO, "300", "luis@agroconecta.co"));

        PageResult<ProductCatalogItem> result = productAdapter.searchActive("agotado", null, PageQuery.of(0, 12));

        assertThat(result.content()).isEmpty();
    }
}
