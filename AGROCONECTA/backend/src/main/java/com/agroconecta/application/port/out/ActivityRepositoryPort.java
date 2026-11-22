package com.agroconecta.application.port.out;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.AgriculturalActivity;

import java.util.List;
import java.util.Optional;

public interface ActivityRepositoryPort {

    Optional<AgriculturalActivity> findById(Long id);

    AgriculturalActivity save(AgriculturalActivity activity);

    void deleteById(Long id);

    PageResult<AgriculturalActivity> findByOwnerId(Long ownerId, Long cropId, PageQuery pageQuery);

    List<AgriculturalActivity> findRecentByOwnerId(Long ownerId, int limit);

    long countByOwnerId(Long ownerId);

    boolean belongsToOwner(Long activityId, Long ownerId);
}
