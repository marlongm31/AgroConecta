package com.agroconecta.application.command;

import com.agroconecta.domain.model.SoilAssessment;

public record SoilAssessmentCommand(
        Long parcelId,
        SoilAssessment.Texture texture,
        SoilAssessment.Drainage drainage,
        SoilAssessment.SoilColor color,
        SoilAssessment.WaterRetention waterRetention,
        SoilAssessment.Slope slope,
        SoilAssessment.PhLevel phLevel,
        SoilAssessment.OrganicMatter organicMatter,
        boolean compaction,
        boolean salinity,
        boolean erosion
) {
}
