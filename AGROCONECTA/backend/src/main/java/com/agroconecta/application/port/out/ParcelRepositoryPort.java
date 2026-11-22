package com.agroconecta.application.port.out;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.Parcel;

import java.util.Optional;

public interface ParcelRepositoryPort {

    Optional<Parcel> findById(Long id);

    Parcel save(Parcel parcel);

    void deleteById(Long id);

    PageResult<Parcel> findByOwnerId(Long ownerId, PageQuery pageQuery);

    long countByOwnerId(Long ownerId);

    boolean belongsToOwner(Long parcelId, Long ownerId);
}
