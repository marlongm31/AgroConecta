package com.agroconecta.application.port.in;

import com.agroconecta.application.command.SoilAssessmentCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.SoilAssessment;

public interface SoilUseCase {

    SoilAssessment assess(Long ownerId, SoilAssessmentCommand command);

    PageResult<SoilAssessment> list(Long ownerId, PageQuery pageQuery);

    SoilAssessment get(Long ownerId, Long assessmentId);
}
