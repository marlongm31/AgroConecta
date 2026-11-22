package com.agroconecta.application.usecase;

import com.agroconecta.application.command.SoilAssessmentCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.in.SoilUseCase;
import com.agroconecta.application.port.out.ParcelRepositoryPort;
import com.agroconecta.application.port.out.SoilAssessmentRepositoryPort;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.model.Parcel;
import com.agroconecta.domain.model.SoilAssessment;
import com.agroconecta.domain.service.SoilAdvisor;

import java.time.Instant;

public class SoilService implements SoilUseCase {

    private final SoilAssessmentRepositoryPort repository;
    private final ParcelRepositoryPort parcelRepository;
    private final SoilAdvisor advisor;

    public SoilService(SoilAssessmentRepositoryPort repository, ParcelRepositoryPort parcelRepository,
                       SoilAdvisor advisor) {
        this.repository = repository;
        this.parcelRepository = parcelRepository;
        this.advisor = advisor;
    }

    @Override
    public SoilAssessment assess(Long ownerId, SoilAssessmentCommand command) {
        if (command.parcelId() != null) {
            Parcel parcel = parcelRepository.findById(command.parcelId())
                    .orElseThrow(() -> new NotFoundException("La parcela indicada no existe"));
            if (!parcel.belongsTo(ownerId)) {
                throw new com.agroconecta.domain.exception.ForbiddenException(
                        "No puedes analizar el suelo de una parcela ajena");
            }
        }
        SoilAdvisor.SoilAnalysis analysis = advisor.analyze(command.texture(), command.drainage(),
                command.color(), command.waterRetention(), command.slope(), command.phLevel(),
                command.organicMatter(), command.compaction(), command.salinity(), command.erosion());
        SoilAssessment assessment = new SoilAssessment(null, ownerId, command.parcelId(), command.texture(),
                command.drainage(), command.color(), command.waterRetention(), command.slope(), command.phLevel(),
                command.organicMatter(), command.compaction(), command.salinity(), command.erosion(),
                analysis.soilType(), analysis.fertilityScore(), analysis.recommendedCrops(),
                analysis.discouragedCrops(), analysis.cropCare(), analysis.amendments(),
                analysis.warnings(), Instant.now());
        return repository.save(assessment);
    }

    @Override
    public PageResult<SoilAssessment> list(Long ownerId, PageQuery pageQuery) {
        return repository.findByOwnerId(ownerId, pageQuery);
    }

    @Override
    public SoilAssessment get(Long ownerId, Long assessmentId) {
        return repository.findByIdAndOwnerId(assessmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("El análisis de suelo no existe"));
    }
}
