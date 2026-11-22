package com.agroconecta.application.port.out;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.SoilAssessment;

import java.util.Optional;

public interface SoilAssessmentRepositoryPort {

    SoilAssessment save(SoilAssessment assessment);

    Optional<SoilAssessment> findByIdAndOwnerId(Long id, Long ownerId);

    PageResult<SoilAssessment> findByOwnerId(Long ownerId, PageQuery pageQuery);
}
