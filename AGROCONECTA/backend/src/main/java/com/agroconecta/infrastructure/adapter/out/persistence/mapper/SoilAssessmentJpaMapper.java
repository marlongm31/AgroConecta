package com.agroconecta.infrastructure.adapter.out.persistence.mapper;

import com.agroconecta.domain.model.SoilAssessment;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.SoilAssessmentJpaEntity;

import java.util.Arrays;
import java.util.List;

public final class SoilAssessmentJpaMapper {

    private SoilAssessmentJpaMapper() {
    }

    public static SoilAssessment toDomain(SoilAssessmentJpaEntity entity) {
        return new SoilAssessment(entity.getId(), entity.getOwnerId(), entity.getParcelId(), entity.getTexture(),
                entity.getDrainage(), entity.getColor(), entity.getWaterRetention(), entity.getSlope(),
                entity.getPhLevel(), entity.getOrganicMatter(), entity.isCompaction(), entity.isSalinity(),
                entity.isErosion(), entity.getSoilType(), entity.getFertilityScore(),
                split(entity.getRecommendedCrops()), split(entity.getDiscouragedCrops()),
                split(entity.getCropCare()), split(entity.getAmendments()), split(entity.getWarnings()),
                entity.getCreatedAt());
    }

    public static SoilAssessmentJpaEntity toEntity(SoilAssessment assessment) {
        SoilAssessmentJpaEntity entity = new SoilAssessmentJpaEntity();
        entity.setId(assessment.id());
        entity.setOwnerId(assessment.ownerId());
        entity.setParcelId(assessment.parcelId());
        entity.setTexture(assessment.texture());
        entity.setDrainage(assessment.drainage());
        entity.setColor(assessment.color());
        entity.setWaterRetention(assessment.waterRetention());
        entity.setSlope(assessment.slope());
        entity.setPhLevel(assessment.phLevel());
        entity.setOrganicMatter(assessment.organicMatter());
        entity.setCompaction(assessment.compacted());
        entity.setSalinity(assessment.saline());
        entity.setErosion(assessment.erosion());
        entity.setSoilType(assessment.soilType());
        entity.setFertilityScore(assessment.fertilityScore());
        entity.setRecommendedCrops(join(assessment.recommendedCrops()));
        entity.setDiscouragedCrops(join(assessment.discouragedCrops()));
        entity.setCropCare(join(assessment.cropCare()));
        entity.setAmendments(join(assessment.amendments()));
        entity.setWarnings(join(assessment.warnings()));
        entity.setCreatedAt(assessment.createdAt());
        return entity;
    }

    private static List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("\n")).filter(line -> !line.isBlank()).toList();
    }

    private static String join(List<String> values) {
        return values == null ? "" : String.join("\n", values);
    }
}
