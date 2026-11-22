package com.agroconecta.application.usecase;

import com.agroconecta.application.command.ActivityCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.in.ActivityUseCase;
import com.agroconecta.application.port.out.ActivityRepositoryPort;
import com.agroconecta.application.port.out.CropRepositoryPort;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.model.AgriculturalActivity;

public class ActivityService implements ActivityUseCase {

    private final ActivityRepositoryPort activityRepository;
    private final CropRepositoryPort cropRepository;

    public ActivityService(ActivityRepositoryPort activityRepository, CropRepositoryPort cropRepository) {
        this.activityRepository = activityRepository;
        this.cropRepository = cropRepository;
    }

    @Override
    public PageResult<AgriculturalActivity> list(Long ownerId, Long cropId, PageQuery pageQuery) {
        if (cropId != null) {
            requireOwnedCrop(ownerId, cropId);
        }
        return activityRepository.findByOwnerId(ownerId, cropId, pageQuery);
    }

    @Override
    public AgriculturalActivity get(Long ownerId, Long activityId) {
        return requireOwnedActivity(ownerId, activityId);
    }

    @Override
    public AgriculturalActivity create(Long ownerId, ActivityCommand command) {
        requireOwnedCrop(ownerId, command.cropId());
        AgriculturalActivity activity = AgriculturalActivity.create(command.cropId(), command.type(),
                command.activityDate(), command.description(), command.observations());
        return activityRepository.save(activity);
    }

    @Override
    public AgriculturalActivity update(Long ownerId, Long activityId, ActivityCommand command) {
        AgriculturalActivity activity = requireOwnedActivity(ownerId, activityId);
        requireOwnedCrop(ownerId, command.cropId());
        AgriculturalActivity updated = activity.update(command.cropId(), command.type(), command.activityDate(),
                command.description(), command.observations());
        return activityRepository.save(updated);
    }

    @Override
    public void delete(Long ownerId, Long activityId) {
        requireOwnedActivity(ownerId, activityId);
        activityRepository.deleteById(activityId);
    }

    private AgriculturalActivity requireOwnedActivity(Long ownerId, Long activityId) {
        AgriculturalActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Actividad agrícola", activityId));
        if (!activityRepository.belongsToOwner(activityId, ownerId)) {
            throw new ForbiddenException("No tienes permiso para acceder a esta actividad");
        }
        return activity;
    }

    private void requireOwnedCrop(Long ownerId, Long cropId) {
        if (cropId == null || !cropRepository.belongsToOwner(cropId, ownerId)) {
            throw new ForbiddenException("El cultivo indicado no existe o no te pertenece");
        }
    }
}
