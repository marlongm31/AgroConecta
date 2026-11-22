package com.agroconecta.application.port.in;

import com.agroconecta.application.command.ActivityCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.AgriculturalActivity;

public interface ActivityUseCase {

    PageResult<AgriculturalActivity> list(Long ownerId, Long cropId, PageQuery pageQuery);

    AgriculturalActivity get(Long ownerId, Long activityId);

    AgriculturalActivity create(Long ownerId, ActivityCommand command);

    AgriculturalActivity update(Long ownerId, Long activityId, ActivityCommand command);

    void delete(Long ownerId, Long activityId);
}
