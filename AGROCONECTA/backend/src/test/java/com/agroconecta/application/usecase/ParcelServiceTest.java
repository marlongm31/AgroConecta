package com.agroconecta.application.usecase;

import com.agroconecta.application.command.ParcelCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.out.ParcelRepositoryPort;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.model.Parcel;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParcelService: CRUD de parcelas con control de propiedad")
class ParcelServiceTest {

    @Mock
    private ParcelRepositoryPort parcelRepository;

    private ParcelService parcelService;

    @BeforeEach
    void setUp() {
        parcelService = new ParcelService(parcelRepository);
    }

    @Test
    @DisplayName("Crea una parcela asignada al productor autenticado")
    void createsParcel() {
        ParcelCommand command = new ParcelCommand("Finca La Esperanza", "Valledupar", new BigDecimal("3.50"), "Riego");
        when(parcelRepository.save(any(Parcel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Parcel parcel = parcelService.create(2L, command);

        assertThat(parcel.ownerId()).isEqualTo(2L);
        assertThat(parcel.name()).isEqualTo("Finca La Esperanza");
    }

    @Test
    @DisplayName("Lista las parcelas del productor")
    void listsParcels() {
        PageResult<Parcel> page = new PageResult<>(List.of(
                Parcel.create(2L, "Finca", "Valledupar", BigDecimal.ONE, null)), 0, 10, 1, 1);
        when(parcelRepository.findByOwnerId(2L, PageQuery.of(0, 10))).thenReturn(page);

        PageResult<Parcel> result = parcelService.list(2L, PageQuery.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lanza NotFound cuando la parcela no existe")
    void notFound() {
        when(parcelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parcelService.get(2L, 99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Impide que un productor acceda a la parcela de otro")
    void forbidsForeignParcel() {
        Parcel parcel = new Parcel(1L, 5L, "Ajena", "Santa Marta", BigDecimal.ONE, null, null, null);
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));

        assertThatThrownBy(() -> parcelService.get(2L, 1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Actualiza una parcela propia")
    void updatesOwnParcel() {
        Parcel parcel = new Parcel(1L, 2L, "Finca", "Valledupar", BigDecimal.ONE, null, null, null);
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.save(any(Parcel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Parcel updated = parcelService.update(2L, 1L,
                new ParcelCommand("Finca Nueva", "La Paz", new BigDecimal("4.00"), "Actualizada"));

        assertThat(updated.name()).isEqualTo("Finca Nueva");
        assertThat(updated.area()).isEqualByComparingTo("4.00");
    }

    @Test
    @DisplayName("No elimina parcelas ajenas")
    void doesNotDeleteForeignParcel() {
        Parcel parcel = new Parcel(1L, 5L, "Ajena", "Santa Marta", BigDecimal.ONE, null, null, null);
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));

        assertThatThrownBy(() -> parcelService.delete(2L, 1L))
                .isInstanceOf(ForbiddenException.class);
        verify(parcelRepository, never()).deleteById(1L);
    }
}
