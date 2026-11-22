package com.agroconecta.application.usecase;

import com.agroconecta.application.model.BuyerDashboard;
import com.agroconecta.application.model.ProducerDashboard;
import com.agroconecta.application.port.in.DashboardUseCase;
import com.agroconecta.application.port.out.ActivityRepositoryPort;
import com.agroconecta.application.port.out.CropRepositoryPort;
import com.agroconecta.application.port.out.ExpenseRepositoryPort;
import com.agroconecta.application.port.out.ParcelRepositoryPort;
import com.agroconecta.application.port.out.ProductRepositoryPort;
import com.agroconecta.application.port.out.ProductionRepositoryPort;

public class DashboardService implements DashboardUseCase {

    private static final int RECENT_ACTIVITY_LIMIT = 6;
    private static final int RECENT_PRODUCT_LIMIT = 6;

    private final ParcelRepositoryPort parcelRepository;
    private final CropRepositoryPort cropRepository;
    private final ExpenseRepositoryPort expenseRepository;
    private final ProductionRepositoryPort productionRepository;
    private final ProductRepositoryPort productRepository;
    private final ActivityRepositoryPort activityRepository;

    public DashboardService(ParcelRepositoryPort parcelRepository,
                            CropRepositoryPort cropRepository,
                            ExpenseRepositoryPort expenseRepository,
                            ProductionRepositoryPort productionRepository,
                            ProductRepositoryPort productRepository,
                            ActivityRepositoryPort activityRepository) {
        this.parcelRepository = parcelRepository;
        this.cropRepository = cropRepository;
        this.expenseRepository = expenseRepository;
        this.productionRepository = productionRepository;
        this.productRepository = productRepository;
        this.activityRepository = activityRepository;
    }

    @Override
    public ProducerDashboard producerDashboard(Long ownerId) {
        return new ProducerDashboard(
                parcelRepository.countByOwnerId(ownerId),
                cropRepository.countActiveByOwnerId(ownerId),
                expenseRepository.sumAmountByOwnerId(ownerId),
                productionRepository.countByOwnerId(ownerId),
                productRepository.countActiveByOwnerId(ownerId),
                activityRepository.findRecentByOwnerId(ownerId, RECENT_ACTIVITY_LIMIT)
        );
    }

    @Override
    public BuyerDashboard buyerDashboard() {
        return new BuyerDashboard(
                productRepository.countActive(),
                productRepository.findRecentActive(RECENT_PRODUCT_LIMIT)
        );
    }
}
