package com.agroconecta.application.usecase;

import com.agroconecta.application.command.ProductionCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.in.ProductionUseCase;
import com.agroconecta.application.port.out.CropRepositoryPort;
import com.agroconecta.application.port.out.ProductionRepositoryPort;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.model.ProductionRecord;

public class ProductionService implements ProductionUseCase {

    private final ProductionRepositoryPort productionRepository;
    private final CropRepositoryPort cropRepository;

    public ProductionService(ProductionRepositoryPort productionRepository, CropRepositoryPort cropRepository) {
        this.productionRepository = productionRepository;
        this.cropRepository = cropRepository;
    }

    @Override
    public PageResult<ProductionRecord> list(Long ownerId, PageQuery pageQuery) {
        return productionRepository.findByOwnerId(ownerId, pageQuery);
    }

    @Override
    public ProductionRecord get(Long ownerId, Long productionId) {
        return requireOwnedProduction(ownerId, productionId);
    }

    @Override
    public ProductionRecord create(Long ownerId, ProductionCommand command) {
        requireOwnedCrop(ownerId, command.cropId());
        ProductionRecord record = ProductionRecord.create(command.cropId(), command.quantity(), command.unit(),
                command.harvestDate(), command.observations());
        return productionRepository.save(record);
    }

    @Override
    public ProductionRecord update(Long ownerId, Long productionId, ProductionCommand command) {
        ProductionRecord record = requireOwnedProduction(ownerId, productionId);
        requireOwnedCrop(ownerId, command.cropId());
        ProductionRecord updated = record.update(command.cropId(), command.quantity(), command.unit(),
                command.harvestDate(), command.observations());
        return productionRepository.save(updated);
    }

    @Override
    public void delete(Long ownerId, Long productionId) {
        requireOwnedProduction(ownerId, productionId);
        productionRepository.deleteById(productionId);
    }

    private ProductionRecord requireOwnedProduction(Long ownerId, Long productionId) {
        ProductionRecord record = productionRepository.findById(productionId)
                .orElseThrow(() -> new NotFoundException("Registro de producción", productionId));
        if (!cropRepository.belongsToOwner(record.cropId(), ownerId)) {
            throw new ForbiddenException("No tienes permiso para acceder a este registro de producción");
        }
        return record;
    }

    private void requireOwnedCrop(Long ownerId, Long cropId) {
        if (cropId == null || !cropRepository.belongsToOwner(cropId, ownerId)) {
            throw new ForbiddenException("El cultivo indicado no existe o no te pertenece");
        }
    }
}
