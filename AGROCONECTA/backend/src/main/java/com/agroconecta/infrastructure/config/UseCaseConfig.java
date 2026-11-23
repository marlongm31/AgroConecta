package com.agroconecta.infrastructure.config;

import com.agroconecta.application.port.in.ActivityUseCase;
import com.agroconecta.application.port.in.AuthUseCase;
import com.agroconecta.application.port.in.ChatUseCase;
import com.agroconecta.application.port.in.CropUseCase;
import com.agroconecta.application.port.in.DashboardUseCase;
import com.agroconecta.application.port.in.ExpenseUseCase;
import com.agroconecta.application.port.in.ParcelUseCase;
import com.agroconecta.application.port.in.ProductImageUseCase;
import com.agroconecta.application.port.in.ProductUseCase;
import com.agroconecta.application.port.in.ProductionUseCase;
import com.agroconecta.application.port.in.SoilUseCase;
import com.agroconecta.application.port.in.UserUseCase;
import com.agroconecta.application.port.in.WeatherUseCase;
import com.agroconecta.application.port.out.ActivityRepositoryPort;
import com.agroconecta.application.port.out.ConversationRepositoryPort;
import com.agroconecta.application.port.out.CropRepositoryPort;
import com.agroconecta.application.port.out.ExpenseRepositoryPort;
import com.agroconecta.application.port.out.FileStoragePort;
import com.agroconecta.application.port.out.MessageRepositoryPort;
import com.agroconecta.application.port.out.ParcelRepositoryPort;
import com.agroconecta.application.port.out.PasswordHasherPort;
import com.agroconecta.application.port.out.ProcessedOperationRepositoryPort;
import com.agroconecta.application.port.out.ProductImageRepositoryPort;
import com.agroconecta.application.port.out.ProductRepositoryPort;
import com.agroconecta.application.port.out.ProductionRepositoryPort;
import com.agroconecta.application.port.out.SoilAssessmentRepositoryPort;
import com.agroconecta.application.port.out.TokenProviderPort;
import com.agroconecta.application.port.out.UserRepositoryPort;
import com.agroconecta.application.port.out.WeatherProviderPort;
import com.agroconecta.application.usecase.ActivityService;
import com.agroconecta.application.usecase.AuthService;
import com.agroconecta.application.usecase.ChatService;
import com.agroconecta.application.usecase.CropService;
import com.agroconecta.application.usecase.DashboardService;
import com.agroconecta.application.usecase.ExpenseService;
import com.agroconecta.application.usecase.IdempotencyService;
import com.agroconecta.application.usecase.ParcelService;
import com.agroconecta.application.usecase.ProductImageService;
import com.agroconecta.application.usecase.ProductService;
import com.agroconecta.application.usecase.ProductionService;
import com.agroconecta.application.usecase.SoilService;
import com.agroconecta.application.usecase.UserService;
import com.agroconecta.application.usecase.WeatherService;
import com.agroconecta.domain.service.SoilAdvisor;
import com.agroconecta.domain.service.WeatherAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public AuthUseCase authUseCase(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher,
                                   TokenProviderPort tokenProvider) {
        return new AuthService(userRepository, passwordHasher, tokenProvider);
    }

    @Bean
    public UserUseCase userUseCase(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher,
                                   FileStoragePort fileStorage) {
        return new UserService(userRepository, passwordHasher, fileStorage);
    }

    @Bean
    public ParcelUseCase parcelUseCase(ParcelRepositoryPort parcelRepository) {
        return new ParcelService(parcelRepository);
    }

    @Bean
    public CropUseCase cropUseCase(CropRepositoryPort cropRepository, ParcelRepositoryPort parcelRepository) {
        return new CropService(cropRepository, parcelRepository);
    }

    @Bean
    public ActivityUseCase activityUseCase(ActivityRepositoryPort activityRepository,
                                           CropRepositoryPort cropRepository) {
        return new ActivityService(activityRepository, cropRepository);
    }

    @Bean
    public ExpenseUseCase expenseUseCase(ExpenseRepositoryPort expenseRepository,
                                         ParcelRepositoryPort parcelRepository,
                                         CropRepositoryPort cropRepository) {
        return new ExpenseService(expenseRepository, parcelRepository, cropRepository);
    }

    @Bean
    public ProductionUseCase productionUseCase(ProductionRepositoryPort productionRepository,
                                               CropRepositoryPort cropRepository) {
        return new ProductionService(productionRepository, cropRepository);
    }

    @Bean
    public ProductUseCase productUseCase(ProductRepositoryPort productRepository) {
        return new ProductService(productRepository);
    }

    @Bean
    public SoilAdvisor soilAdvisor() {
        return new SoilAdvisor();
    }

    @Bean
    public SoilUseCase soilUseCase(SoilAssessmentRepositoryPort assessmentRepository,
                                   ParcelRepositoryPort parcelRepository,
                                   SoilAdvisor soilAdvisor) {
        return new SoilService(assessmentRepository, parcelRepository, soilAdvisor);
    }

    @Bean
    public IdempotencyService idempotencyService(ProcessedOperationRepositoryPort operationRepository) {
        return new IdempotencyService(operationRepository);
    }

    @Bean
    public ChatUseCase chatUseCase(ConversationRepositoryPort conversationRepository,
                                   MessageRepositoryPort messageRepository,
                                   ProductRepositoryPort productRepository,
                                   UserRepositoryPort userRepository,
                                   FileStoragePort fileStorage,
                                   IdempotencyService idempotencyService) {
        return new ChatService(conversationRepository, messageRepository, productRepository, userRepository,
                fileStorage, idempotencyService);
    }

    @Bean
    public WeatherAdvisor weatherAdvisor() {
        return new WeatherAdvisor();
    }

    @Bean
    public WeatherUseCase weatherUseCase(WeatherProviderPort weatherProvider,
                                         ParcelRepositoryPort parcelRepository,
                                         CropRepositoryPort cropRepository,
                                         WeatherAdvisor weatherAdvisor) {
        return new WeatherService(weatherProvider, parcelRepository, cropRepository, weatherAdvisor);
    }

    @Bean
    public ProductImageUseCase productImageUseCase(ProductRepositoryPort productRepository,
                                                   ProductImageRepositoryPort imageRepository,
                                                   FileStoragePort fileStorage) {
        return new ProductImageService(productRepository, imageRepository, fileStorage);
    }

    @Bean
    public DashboardUseCase dashboardUseCase(ParcelRepositoryPort parcelRepository,
                                             CropRepositoryPort cropRepository,
                                             ExpenseRepositoryPort expenseRepository,
                                             ProductionRepositoryPort productionRepository,
                                             ProductRepositoryPort productRepository,
                                             ActivityRepositoryPort activityRepository) {
        return new DashboardService(parcelRepository, cropRepository, expenseRepository,
                productionRepository, productRepository, activityRepository);
    }
}
