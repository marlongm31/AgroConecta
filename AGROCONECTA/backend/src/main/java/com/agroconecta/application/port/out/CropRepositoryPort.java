package com.agroconecta.application.port.out;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.Crop;

import java.util.Optional;

public interface CropRepositoryPort {

    Optional<Crop> findById(Long id);

    Crop save(Crop crop);

    void deleteById(Long id);

    PageResult<Crop> findByOwnerId(Long ownerId, Long parcelId, PageQuery pageQuery);

    long countActiveByOwnerId(Long ownerId);

    boolean belongsToOwner(Long cropId, Long ownerId);
}
