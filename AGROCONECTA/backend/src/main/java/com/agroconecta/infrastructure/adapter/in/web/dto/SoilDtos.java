package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.application.command.SoilAssessmentCommand;
import com.agroconecta.domain.model.SoilAssessment;
import com.agroconecta.domain.service.SoilAdvisor;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public final class SoilDtos {

    private SoilDtos() {
    }

    public record SoilAssessmentRequest(
            Long parcelId,
            @NotNull SoilAssessment.Texture texture,
            @NotNull SoilAssessment.Drainage drainage,
            @NotNull SoilAssessment.SoilColor color,
            @NotNull SoilAssessment.WaterRetention waterRetention,
            @NotNull SoilAssessment.Slope slope,
            @NotNull SoilAssessment.PhLevel phLevel,
            @NotNull SoilAssessment.OrganicMatter organicMatter,
            boolean compaction,
            boolean salinity,
            boolean erosion
    ) {

        public SoilAssessmentCommand toCommand() {
            return new SoilAssessmentCommand(parcelId, texture, drainage, color, waterRetention, slope,
                    phLevel, organicMatter, compaction, salinity, erosion);
        }
    }

    public record SoilAssessmentResponse(
            Long id,
            Long parcelId,
            String texture,
            String textureLabel,
            String drainage,
            String drainageLabel,
            String color,
            String colorLabel,
            String waterRetention,
            String waterRetentionLabel,
            String slope,
            String slopeLabel,
            String phLevel,
            String phLevelLabel,
            String organicMatter,
            String organicMatterLabel,
            boolean compaction,
            boolean salinity,
            boolean erosion,
            String soilType,
            int fertilityScore,
            String fertilityLabel,
            List<String> recommendedCrops,
            List<String> discouragedCrops,
            List<String> cropCare,
            List<String> amendments,
            List<String> warnings,
            String disclaimer,
            Instant createdAt
    ) {

        public static SoilAssessmentResponse from(SoilAssessment assessment) {
            return new SoilAssessmentResponse(assessment.id(), assessment.parcelId(),
                    assessment.texture().name(), assessment.texture().label(),
                    assessment.drainage().name(), assessment.drainage().label(),
                    assessment.color().name(), assessment.color().label(),
                    assessment.waterRetention().name(), assessment.waterRetention().label(),
                    assessment.slope().name(), assessment.slope().label(),
                    assessment.phLevel().name(), assessment.phLevel().label(),
                    assessment.organicMatter().name(), assessment.organicMatter().label(),
                    assessment.compacted(), assessment.saline(), assessment.erosion(), assessment.soilType(),
                    assessment.fertilityScore(), fertilityLabel(assessment.fertilityScore()),
                    assessment.recommendedCrops(), assessment.discouragedCrops(), assessment.cropCare(),
                    assessment.amendments(), assessment.warnings(),
                    SoilAdvisor.DISCLAIMER, assessment.createdAt());
        }

        private static String fertilityLabel(int score) {
            if (score >= 75) {
                return "Alta";
            }
            if (score >= 50) {
                return "Media";
            }
            return "Baja";
        }
    }
}
