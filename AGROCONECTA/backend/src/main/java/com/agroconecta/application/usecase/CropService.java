package com.agroconecta.application.usecase;

import com.agroconecta.application.command.CropCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.in.CropUseCase;
import com.agroconecta.application.port.out.CropRepositoryPort;
import com.agroconecta.application.port.out.ParcelRepositoryPort;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.model.Crop;
import com.agroconecta.domain.model.CropStatus;

public class CropService implements CropUseCase {

    private final CropRepositoryPort cropRepository;
    private final ParcelRepositoryPort parcelRepository;

    public CropService(CropRepositoryPort cropRepository, ParcelRepositoryPort parcelRepository) {
        this.cropRepository = cropRepository;
        this.parcelRepository = parcelRepository;
    }

    @Override
    public PageResult<Crop> list(Long ownerId, Long parcelId, PageQuery pageQuery) {
        if (parcelId != null) {
            requireOwnedParcel(ownerId, parcelId);
        }
        return cropRepository.findByOwnerId(ownerId, parcelId, pageQuery);
    }

    @Override
    public Crop get(Long ownerId, Long cropId) {
        return requireOwnedCrop(ownerId, cropId);
    }

    @Override
    public Crop create(Long ownerId, CropCommand command) {
        requireOwnedParcel(ownerId, command.parcelId());
        Crop crop = Crop.create(command.parcelId(), command.name(), command.variety(), command.plantingDate(),
                command.estimatedHarvestDate(), command.status(), command.observations());
        return cropRepository.save(crop);
    }

    @Override
    public Crop update(Long ownerId, Long cropId, CropCommand command) {
        Crop crop = requireOwnedCrop(ownerId, cropId);
        requireOwnedParcel(ownerId, command.parcelId());
        Crop updated = crop.update(command.parcelId(), command.name(), command.variety(), command.plantingDate(),
                command.estimatedHarvestDate(), command.status(), command.observations());
        return cropRepository.save(updated);
    }

    @Override
    public Crop changeStatus(Long ownerId, Long cropId, CropStatus status) {
        Crop crop = requireOwnedCrop(ownerId, cropId);
        return cropRepository.save(crop.changeStatus(status));
    }

    @Override
    public void delete(Long ownerId, Long cropId) {
        requireOwnedCrop(ownerId, cropId);
        cropRepository.deleteById(cropId);
    }

    private Crop requireOwnedCrop(Long ownerId, Long cropId) {
        Crop crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new NotFoundException("Cultivo", cropId));
        if (!cropRepository.belongsToOwner(cropId, ownerId)) {
            throw new ForbiddenException("No tienes permiso para acceder a este cultivo");
        }
        return crop;
    }

    private void requireOwnedParcel(Long ownerId, Long parcelId) {
        if (parcelId == null || !parcelRepository.belongsToOwner(parcelId, ownerId)) {
            throw new ForbiddenException("La parcela indicada no existe o no te pertenece");
        }
    }
}
