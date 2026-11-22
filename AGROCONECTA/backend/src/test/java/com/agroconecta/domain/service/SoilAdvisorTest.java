package com.agroconecta.domain.service;

import com.agroconecta.domain.model.SoilAssessment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SoilAdvisor: orientación agronómica basada en observaciones")
class SoilAdvisorTest {

    private final SoilAdvisor advisor = new SoilAdvisor();

    @Test
    @DisplayName("identifica cultivos desaconsejados por drenaje, salinidad y acidez")
    void discouragesUnsuitableCrops() {
        SoilAdvisor.SoilAnalysis analysis = advisor.analyze(
                SoilAssessment.Texture.ARCILLOSO,
                SoilAssessment.Drainage.ANEGADO,
                SoilAssessment.SoilColor.OSCURO,
                SoilAssessment.WaterRetention.ALTA,
                SoilAssessment.Slope.PLANO,
                SoilAssessment.PhLevel.MUY_ACIDO,
                SoilAssessment.OrganicMatter.BAJA,
                true, true, true);

        assertThat(analysis.discouragedCrops()).anySatisfy(item ->
                assertThat(item).contains("Maní").contains("humedad"));
        assertThat(analysis.discouragedCrops()).anySatisfy(item ->
                assertThat(item).contains("Fríjol").contains("salinidad"));
        assertThat(analysis.discouragedCrops()).anySatisfy(item ->
                assertThat(item).contains("Alfalfa").contains("pH"));
    }

    @Test
    @DisplayName("describe cuidados para cada cultivo recomendado")
    void describesCareForRecommendedCrops() {
        SoilAdvisor.SoilAnalysis analysis = advisor.analyze(
                SoilAssessment.Texture.FRANCO,
                SoilAssessment.Drainage.MODERADO,
                SoilAssessment.SoilColor.OSCURO,
                SoilAssessment.WaterRetention.MEDIA,
                SoilAssessment.Slope.PLANO,
                SoilAssessment.PhLevel.OPTIMO,
                SoilAssessment.OrganicMatter.MEDIA,
                false, false, false);

        assertThat(analysis.recommendedCrops()).isNotEmpty();
        assertThat(analysis.cropCare()).hasSize(analysis.recommendedCrops().size());
        assertThat(analysis.cropCare()).allSatisfy(item -> assertThat(item).contains(":"));
        assertThat(analysis.discouragedCrops()).isEmpty();
    }
}
