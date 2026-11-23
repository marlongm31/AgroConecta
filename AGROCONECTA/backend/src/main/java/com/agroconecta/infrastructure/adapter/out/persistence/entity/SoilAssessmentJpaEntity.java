package com.agroconecta.infrastructure.adapter.out.persistence.entity;

import com.agroconecta.domain.model.SoilAssessment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "soil_assessments")
public class SoilAssessmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "parcel_id")
    private Long parcelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SoilAssessment.Texture texture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SoilAssessment.Drainage drainage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SoilAssessment.SoilColor color;

    @Enumerated(EnumType.STRING)
    @Column(name = "water_retention", nullable = false, length = 30)
    private SoilAssessment.WaterRetention waterRetention;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SoilAssessment.Slope slope;

    @Enumerated(EnumType.STRING)
    @Column(name = "ph_level", nullable = false, length = 30)
    private SoilAssessment.PhLevel phLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "organic_matter", nullable = false, length = 30)
    private SoilAssessment.OrganicMatter organicMatter;

    @Column(nullable = false)
    private boolean compaction;

    @Column(nullable = false)
    private boolean salinity;

    @Column(nullable = false)
    private boolean erosion;

    @Column(name = "soil_type", nullable = false, length = 80)
    private String soilType;

    @Column(name = "fertility_score", nullable = false)
    private int fertilityScore;

    @Column(name = "recommended_crops", nullable = false, columnDefinition = "text")
    private String recommendedCrops;

    @Column(name = "discouraged_crops", nullable = false, columnDefinition = "text")
    private String discouragedCrops;

    @Column(name = "crop_care", nullable = false, columnDefinition = "text")
    private String cropCare;

    @Column(nullable = false, columnDefinition = "text")
    private String amendments;

    @Column(nullable = false, columnDefinition = "text")
    private String warnings;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getParcelId() {
        return parcelId;
    }

    public void setParcelId(Long parcelId) {
        this.parcelId = parcelId;
    }

    public SoilAssessment.Texture getTexture() {
        return texture;
    }

    public void setTexture(SoilAssessment.Texture texture) {
        this.texture = texture;
    }

    public SoilAssessment.Drainage getDrainage() {
        return drainage;
    }

    public void setDrainage(SoilAssessment.Drainage drainage) {
        this.drainage = drainage;
    }

    public SoilAssessment.SoilColor getColor() {
        return color;
    }

    public void setColor(SoilAssessment.SoilColor color) {
        this.color = color;
    }

    public SoilAssessment.WaterRetention getWaterRetention() {
        return waterRetention;
    }

    public void setWaterRetention(SoilAssessment.WaterRetention waterRetention) {
        this.waterRetention = waterRetention;
    }

    public SoilAssessment.Slope getSlope() {
        return slope;
    }

    public void setSlope(SoilAssessment.Slope slope) {
        this.slope = slope;
    }

    public SoilAssessment.PhLevel getPhLevel() {
        return phLevel;
    }

    public void setPhLevel(SoilAssessment.PhLevel phLevel) {
        this.phLevel = phLevel;
    }

    public SoilAssessment.OrganicMatter getOrganicMatter() {
        return organicMatter;
    }

    public void setOrganicMatter(SoilAssessment.OrganicMatter organicMatter) {
        this.organicMatter = organicMatter;
    }

    public boolean isCompaction() {
        return compaction;
    }

    public void setCompaction(boolean compaction) {
        this.compaction = compaction;
    }

    public boolean isSalinity() {
        return salinity;
    }

    public void setSalinity(boolean salinity) {
        this.salinity = salinity;
    }

    public boolean isErosion() {
        return erosion;
    }

    public void setErosion(boolean erosion) {
        this.erosion = erosion;
    }

    public String getSoilType() {
        return soilType;
    }

    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }

    public int getFertilityScore() {
        return fertilityScore;
    }

    public void setFertilityScore(int fertilityScore) {
        this.fertilityScore = fertilityScore;
    }

    public String getRecommendedCrops() {
        return recommendedCrops;
    }

    public void setRecommendedCrops(String recommendedCrops) {
        this.recommendedCrops = recommendedCrops;
    }

    public String getDiscouragedCrops() {
        return discouragedCrops;
    }

    public void setDiscouragedCrops(String discouragedCrops) {
        this.discouragedCrops = discouragedCrops;
    }

    public String getCropCare() {
        return cropCare;
    }

    public void setCropCare(String cropCare) {
        this.cropCare = cropCare;
    }

    public String getAmendments() {
        return amendments;
    }

    public void setAmendments(String amendments) {
        this.amendments = amendments;
    }

    public String getWarnings() {
        return warnings;
    }

    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
