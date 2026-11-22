package com.agroconecta.domain.model;

import com.agroconecta.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Modelos de dominio: invariantes de negocio")
class DomainModelTest {

    @Test
    @DisplayName("User.create normaliza el correo y marca la cuenta como activa")
    void userCreation() {
        User user = User.create("María Gómez", " MARIA@AgroConecta.CO ", "hash", Role.PRODUCTOR,
                "+57 300", "Valledupar");

        assertThat(user.email()).isEqualTo("maria@agroconecta.co");
        assertThat(user.active()).isTrue();
        assertThat(user.isProducer()).isTrue();
    }

    @Test
    @DisplayName("Role indica qué roles puede autoasignar un usuario al registrarse")
    void selfAssignableRoles() {
        assertThat(Role.PRODUCTOR.isSelfAssignable()).isTrue();
        assertThat(Role.COMPRADOR.isSelfAssignable()).isTrue();
        assertThat(Role.ADMINISTRADOR.isSelfAssignable()).isFalse();
    }

    @Test
    @DisplayName("Parcel rechaza un área no positiva")
    void parcelRejectsInvalidArea() {
        assertThatThrownBy(() -> Parcel.create(1L, "Finca", "Valledupar", BigDecimal.ZERO, null))
                .isInstanceOf(BusinessRuleException.class);

        Parcel parcel = Parcel.create(1L, "Finca", "Valledupar", new BigDecimal("3.50"), "Riego");
        assertThat(parcel.belongsTo(1L)).isTrue();
        assertThat(parcel.belongsTo(2L)).isFalse();
    }

    @Test
    @DisplayName("Crop rechaza una fecha de cosecha anterior a la siembra")
    void cropRejectsInvalidHarvestDate() {
        LocalDate planting = LocalDate.of(2026, 8, 15);

        assertThatThrownBy(() -> Crop.create(1L, "Tomate", "Chonto", planting, planting.minusDays(1),
                CropStatus.EN_CRECIMIENTO, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("anterior a la siembra");

        Crop crop = Crop.create(1L, "Tomate", "Chonto", planting, planting.plusMonths(3),
                CropStatus.EN_CRECIMIENTO, null);
        assertThat(crop.status().isActive()).isTrue();
    }

    @Test
    @DisplayName("Expense exige estar asociado a una parcela o a un cultivo")
    void expenseRequiresRelation() {
        assertThatThrownBy(() -> Expense.create(1L, null, null, "Insumos", new BigDecimal("1000"),
                LocalDate.now()))
                .isInstanceOf(BusinessRuleException.class);

        Expense expense = Expense.create(1L, 1L, null, "Insumos", new BigDecimal("1000"), LocalDate.now());
        assertThat(expense.amount()).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("Product valida precio positivo y cantidad no negativa")
    void productValidation() {
        assertThatThrownBy(() -> Product.create(1L, "Café", "Descripción", ProductCategory.CAFE,
                BigDecimal.ZERO, BigDecimal.TEN, MeasurementUnit.KILOGRAMO, "300", "a@b.co"))
                .isInstanceOf(BusinessRuleException.class);

        Product product = Product.create(1L, "Café", "Descripción", ProductCategory.CAFE,
                new BigDecimal("18500"), BigDecimal.ZERO, MeasurementUnit.KILOGRAMO, "300", "A@B.CO");
        assertThat(product.active()).isTrue();
        assertThat(product.soldOut()).isTrue();
        assertThat(product.contactEmail()).isEqualTo("a@b.co");
        assertThat(product.withStatus(false).active()).isFalse();
    }

    @Test
    @DisplayName("ProductionRecord exige cantidad positiva y unidad")
    void productionValidation() {
        assertThatThrownBy(() -> ProductionRecord.create(1L, new BigDecimal("-5"), MeasurementUnit.KILOGRAMO,
                LocalDate.now(), null))
                .isInstanceOf(BusinessRuleException.class);

        ProductionRecord record = ProductionRecord.create(1L, new BigDecimal("850"), MeasurementUnit.KILOGRAMO,
                LocalDate.now(), "Cosecha");
        assertThat(record.quantity()).isEqualByComparingTo("850");
    }
}
