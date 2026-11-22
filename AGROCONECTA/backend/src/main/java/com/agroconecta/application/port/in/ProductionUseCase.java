package com.agroconecta.application.port.in;

import com.agroconecta.application.command.ProductionCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.ProductionRecord;

public interface ProductionUseCase {

    PageResult<ProductionRecord> list(Long ownerId, PageQuery pageQuery);

    ProductionRecord get(Long ownerId, Long productionId);

    ProductionRecord create(Long ownerId, ProductionCommand command);

    ProductionRecord update(Long ownerId, Long productionId, ProductionCommand command);

    void delete(Long ownerId, Long productionId);
}
