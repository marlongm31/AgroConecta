package com.agroconecta.application.port.out;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.ProductionRecord;

import java.util.Optional;

public interface ProductionRepositoryPort {

    Optional<ProductionRecord> findById(Long id);

    ProductionRecord save(ProductionRecord record);

    void deleteById(Long id);

    PageResult<ProductionRecord> findByOwnerId(Long ownerId, PageQuery pageQuery);

    long countByOwnerId(Long ownerId);
}
