package com.agroconecta.application.usecase;

import com.agroconecta.application.command.CropCommand;
import com.agroconecta.application.port.out.CropRepositoryPort;
import com.agroconecta.application.port.out.ParcelRepositoryPort;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.model.Crop;
import com.agroconecta.domain.model.CropStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CropService: cultivos y control de propiedad de parcelas")
class CropServiceTest {

    @Mock
    private CropRepositoryPort cropRepository;
    @Mock
    private ParcelRepositoryPort parcelRepository;

    private CropService cropService;

    @BeforeEach
    void setUp() {
        cropService = new CropService(cropRepository, parcelRepository);
    }

    private CropCommand command() {
        return new CropCommand(1L, "Tomate", "Chonto", LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 12, 10), CropStatus.EN_CRECIMIENTO, null);
    }

    @Test
    @DisplayName("Impide crear un cultivo en una parcela ajena")
    void forbidsForeignParcel() {
        when(parcelRepository.belongsToOwner(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> cropService.create(2L, command()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Crea un cultivo en una parcela propia")
    void createsCrop() {
        when(parcelRepository.belongsToOwner(1L, 2L)).thenReturn(true);
        when(cropRepository.save(any(Crop.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Crop crop = cropService.create(2L, command());

        assertThat(crop.parcelId()).isEqualTo(1L);
        assertThat(crop.status()).isEqualTo(CropStatus.EN_CRECIMIENTO);
    }

    @Test
    @DisplayName("Cambia el estado de un cultivo propio")
    void changesStatus() {
        Crop crop = new Crop(1L, 1L, "Tomate", "Chonto", LocalDate.now(), null,
                CropStatus.EN_CRECIMIENTO, null, null, null);
        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));
        when(cropRepository.belongsToOwner(1L, 2L)).thenReturn(true);
        when(cropRepository.save(any(Crop.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Crop result = cropService.changeStatus(2L, 1L, CropStatus.COSECHADO);

        assertThat(result.status()).isEqualTo(CropStatus.COSECHADO);
    }

    @Test
    @DisplayName("Lanza NotFound cuando el cultivo no existe")
    void notFound() {
        when(cropRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cropService.get(2L, 5L))
                .isInstanceOf(NotFoundException.class);
    }
}
