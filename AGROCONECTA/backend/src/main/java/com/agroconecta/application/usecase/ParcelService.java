package com.agroconecta.application.usecase;

import com.agroconecta.application.command.ParcelCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.in.ParcelUseCase;
import com.agroconecta.application.port.out.ParcelRepositoryPort;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.model.Parcel;

public class ParcelService implements ParcelUseCase {

    private final ParcelRepositoryPort parcelRepository;

    public ParcelService(ParcelRepositoryPort parcelRepository) {
        this.parcelRepository = parcelRepository;
    }

    @Override
    public PageResult<Parcel> list(Long ownerId, PageQuery pageQuery) {
        return parcelRepository.findByOwnerId(ownerId, pageQuery);
    }

    @Override
    public Parcel get(Long ownerId, Long parcelId) {
        return requireOwnedParcel(ownerId, parcelId);
    }

    @Override
    public Parcel create(Long ownerId, ParcelCommand command) {
        Parcel parcel = Parcel.create(ownerId, command.name(), command.location(), command.area(), command.description());
        return parcelRepository.save(parcel);
    }

    @Override
    public Parcel update(Long ownerId, Long parcelId, ParcelCommand command) {
        Parcel parcel = requireOwnedParcel(ownerId, parcelId);
        Parcel updated = parcel.update(command.name(), command.location(), command.area(), command.description());
        return parcelRepository.save(updated);
    }

    @Override
    public void delete(Long ownerId, Long parcelId) {
        requireOwnedParcel(ownerId, parcelId);
        parcelRepository.deleteById(parcelId);
    }

    private Parcel requireOwnedParcel(Long ownerId, Long parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new NotFoundException("Parcela", parcelId));
        if (!parcel.belongsTo(ownerId)) {
            throw new ForbiddenException("No tienes permiso para acceder a esta parcela");
        }
        return parcel;
    }
}
