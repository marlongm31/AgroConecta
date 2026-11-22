package com.agroconecta.application.port.in;

import com.agroconecta.application.model.BuyerDashboard;
import com.agroconecta.application.model.ProducerDashboard;

public interface DashboardUseCase {

    ProducerDashboard producerDashboard(Long ownerId);

    BuyerDashboard buyerDashboard();
}
