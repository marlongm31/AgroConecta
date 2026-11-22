package com.agroconecta.application.port.in;

import com.agroconecta.application.command.CropCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.Crop;
import com.agroconecta.domain.model.CropStatus;

public interface CropUseCase {

    PageResult<Crop> list(Long ownerId, Long parcelId, PageQuery pageQuery);

    Crop get(Long ownerId, Long cropId);

    Crop create(Long ownerId, CropCommand command);

    Crop update(Long ownerId, Long cropId, CropCommand command);

    Crop changeStatus(Long ownerId, Long cropId, CropStatus status);

    void delete(Long ownerId, Long cropId);
}
