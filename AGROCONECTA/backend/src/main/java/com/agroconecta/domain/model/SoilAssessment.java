package com.agroconecta.domain.model;

import com.agroconecta.domain.validation.DomainValidator;

import java.time.Instant;
import java.util.List;

public record SoilAssessment(
        Long id,
        Long ownerId,
        Long parcelId,
        Texture texture,
        Drainage drainage,
        SoilColor color,
        WaterRetention waterRetention,
        Slope slope,
        PhLevel phLevel,
        OrganicMatter organicMatter,
        boolean compacted,
        boolean saline,
        boolean erosion,
        String soilType,
        int fertilityScore,
        List<String> recommendedCrops,
        List<String> discouragedCrops,
        List<String> cropCare,
        List<String> amendments,
        List<String> warnings,
        Instant createdAt
) {

    public SoilAssessment {
        DomainValidator.requireId(ownerId, "propietario");
        if (texture == null || drainage == null || color == null || waterRetention == null
                || slope == null || phLevel == null || organicMatter == null) {
            throw new com.agroconecta.domain.exception.BusinessRuleException(
                    "Debes responder todas las preguntas del cuestionario de suelo");
        }
        soilType = DomainValidator.text(soilType, 80, "tipo de suelo");
        recommendedCrops = List.copyOf(recommendedCrops == null ? List.of() : recommendedCrops);
        discouragedCrops = List.copyOf(discouragedCrops == null ? List.of() : discouragedCrops);
        cropCare = List.copyOf(cropCare == null ? List.of() : cropCare);
        amendments = List.copyOf(amendments == null ? List.of() : amendments);
        warnings = List.copyOf(warnings == null ? List.of() : warnings);
        if (fertilityScore < 0 || fertilityScore > 100) {
            throw new com.agroconecta.domain.exception.BusinessRuleException(
                    "El puntaje de fertilidad debe estar entre 0 y 100");
        }
    }

    public boolean belongsTo(Long candidateOwnerId) {
        return ownerId.equals(candidateOwnerId);
    }

    public enum Texture {
        ARENOSO("Arenoso"),
        FRANCO_ARENOSO("Franco arenoso"),
        FRANCO("Franco"),
        FRANCO_ARCILLOSO("Franco arcilloso"),
        ARCILLOSO("Arcilloso"),
        LIMOSO("Limoso"),
        ORGANICO("Orgánico");

        private final String label;

        Texture(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum Drainage {
        RAPIDO("Rápido"),
        MODERADO("Moderado"),
        LENTO("Lento"),
        ANEGADO("Anegado");

        private final String label;

        Drainage(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum SoilColor {
        OSCURO("Oscuro"),
        ROJIZO("Rojizo"),
        CLARO("Claro"),
        AMARILLENTO("Amarillento"),
        GRISACEO("Grisáceo");

        private final String label;

        SoilColor(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum WaterRetention {
        BAJA("Baja"),
        MEDIA("Media"),
        ALTA("Alta");

        private final String label;

        WaterRetention(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum Slope {
        PLANO("Plano"),
        SUAVE("Suave"),
        MODERADO("Moderado"),
        PRONUNCIADO("Pronunciado");

        private final String label;

        Slope(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum PhLevel {
        MUY_ACIDO("Muy ácido (menos de 5,0)"),
        ACIDO("Ácido (5,0 a 6,0)"),
        OPTIMO("Óptimo (6,0 a 7,0)"),
        ALCALINO("Alcalino (7,0 a 8,0)"),
        MUY_ALCALINO("Muy alcalino (más de 8,0)");

        private final String label;

        PhLevel(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum OrganicMatter {
        BAJA("Baja"),
        MEDIA("Media"),
        ALTA("Alta");

        private final String label;

        OrganicMatter(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }
}
