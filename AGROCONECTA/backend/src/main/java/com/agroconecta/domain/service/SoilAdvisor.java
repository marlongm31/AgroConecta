package com.agroconecta.domain.service;

import com.agroconecta.domain.model.SoilAssessment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SoilAdvisor {

    public static final String DISCLAIMER =
            "Esta orientación es informativa y no reemplaza un análisis de laboratorio ni la asesoría de un ingeniero agrónomo.";

    public SoilAnalysis analyze(SoilAssessment.Texture texture, SoilAssessment.Drainage drainage,
                                SoilAssessment.SoilColor color, SoilAssessment.WaterRetention waterRetention,
                                SoilAssessment.Slope slope, SoilAssessment.PhLevel phLevel,
                                SoilAssessment.OrganicMatter organicMatter, boolean compacted,
                                boolean saline, boolean erosion) {
        int score = calculateScore(texture, drainage, color, waterRetention, slope, phLevel,
                organicMatter, compacted, saline, erosion);
        String soilType = texture.label() + " con fertilidad " + fertilityLabel(score).toLowerCase();
        List<String> recommended = recommendedCrops(texture, drainage, phLevel);
        return new SoilAnalysis(score, soilType, recommended,
                discouragedCrops(texture, drainage, waterRetention, phLevel, saline),
                cropCare(recommended, drainage, organicMatter, compacted),
                amendments(drainage, slope, phLevel, organicMatter, compacted, saline, erosion, color),
                warnings(drainage, slope, phLevel, organicMatter, compacted, saline, erosion));
    }

    private int calculateScore(SoilAssessment.Texture texture, SoilAssessment.Drainage drainage,
                               SoilAssessment.SoilColor color, SoilAssessment.WaterRetention waterRetention,
                               SoilAssessment.Slope slope, SoilAssessment.PhLevel phLevel,
                               SoilAssessment.OrganicMatter organicMatter, boolean compacted,
                               boolean saline, boolean erosion) {
        int score = 60;
        score += switch (texture) {
            case FRANCO -> 12;
            case FRANCO_ARENOSO, FRANCO_ARCILLOSO -> 8;
            case ORGANICO -> 10;
            case LIMOSO -> 4;
            case ARENOSO -> -4;
            case ARCILLOSO -> -6;
        };
        score += switch (drainage) {
            case MODERADO -> 6;
            case RAPIDO -> -4;
            case LENTO -> -10;
            case ANEGADO -> -20;
        };
        score += switch (color) {
            case OSCURO -> 5;
            case ROJIZO -> 1;
            case CLARO -> -5;
            case AMARILLENTO -> -3;
            case GRISACEO -> -4;
        };
        score += switch (waterRetention) {
            case MEDIA -> 5;
            case ALTA -> 2;
            case BAJA -> -6;
        };
        score += switch (slope) {
            case PLANO -> 5;
            case SUAVE -> 3;
            case MODERADO -> -3;
            case PRONUNCIADO -> -10;
        };
        score += switch (phLevel) {
            case OPTIMO -> 10;
            case ACIDO, ALCALINO -> 2;
            case MUY_ACIDO, MUY_ALCALINO -> -10;
        };
        score += switch (organicMatter) {
            case ALTA -> 12;
            case MEDIA -> 5;
            case BAJA -> -12;
        };
        if (compacted) {
            score -= 10;
        }
        if (saline) {
            score -= 12;
        }
        if (erosion) {
            score -= 12;
        }
        return Math.max(0, Math.min(100, score));
    }

    private String fertilityLabel(int score) {
        if (score >= 75) {
            return "Alta";
        }
        if (score >= 50) {
            return "Media";
        }
        return "Baja";
    }

    private List<String> recommendedCrops(SoilAssessment.Texture texture, SoilAssessment.Drainage drainage,
                                          SoilAssessment.PhLevel phLevel) {
        Set<String> crops = new LinkedHashSet<>(switch (texture) {
            case ARENOSO -> List.of("Maní", "Yuca", "Sandía", "Melón");
            case FRANCO_ARENOSO -> List.of("Maíz", "Yuca", "Tomate", "Ají");
            case FRANCO -> List.of("Maíz", "Fríjol", "Tomate", "Café", "Plátano");
            case FRANCO_ARCILLOSO -> List.of("Café", "Cacao", "Plátano", "Arroz");
            case ARCILLOSO -> List.of("Arroz", "Cacao", "Caña", "Plátano");
            case LIMOSO -> List.of("Arroz", "Hortalizas", "Maíz");
            case ORGANICO -> List.of("Hortalizas", "Fríjol", "Maíz", "Café");
        });
        switch (phLevel) {
            case MUY_ACIDO, ACIDO -> {
                crops.add("Café");
                crops.add("Cacao");
                crops.add("Piña");
            }
            case ALCALINO, MUY_ALCALINO -> {
                crops.add("Sorgo");
                crops.add("Cebada");
            }
            default -> {
            }
        }
        if (drainage == SoilAssessment.Drainage.ANEGADO) {
            crops.add("Arroz");
        }
        return crops.stream().limit(6).toList();
    }

    private List<String> discouragedCrops(SoilAssessment.Texture texture, SoilAssessment.Drainage drainage,
                                          SoilAssessment.WaterRetention waterRetention,
                                          SoilAssessment.PhLevel phLevel, boolean saline) {
        Set<String> crops = new LinkedHashSet<>();
        if (drainage == SoilAssessment.Drainage.RAPIDO
                || waterRetention == SoilAssessment.WaterRetention.BAJA) {
            crops.add("Arroz: necesita lámina de agua y no tolera la sequía rápida.");
            crops.add("Plátano: requiere humedad constante y suelo profundo.");
        }
        if (drainage == SoilAssessment.Drainage.LENTO
                || drainage == SoilAssessment.Drainage.ANEGADO) {
            crops.add("Maní: el exceso de humedad pudre la semilla y favorece hongos.");
            crops.add("Sandía: el encharcamiento dificulta el desarrollo de raíces.");
            crops.add("Ají: es sensible a la asfixia radicular en suelos mal drenados.");
        }
        if (saline) {
            crops.add("Fríjol: es sensible a la salinidad y reduce su germinación.");
            crops.add("Café: la salinidad limita la absorción de nutrientes.");
        }
        if (phLevel == SoilAssessment.PhLevel.MUY_ACIDO) {
            crops.add("Alfalfa: requiere pH cercano a neutro para establecerse.");
            crops.add("Cebada: no tolera acidez extrema.");
        }
        if (phLevel == SoilAssessment.PhLevel.MUY_ALCALINO) {
            crops.add("Café: en suelos muy alcalinos presenta clorosis y baja productividad.");
            crops.add("Cacao: prefiere suelos ligeramente ácidos y bien drenados.");
            crops.add("Piña: la alcalinidad alta afecta su desarrollo.");
        }
        if (texture == SoilAssessment.Texture.ARCILLOSO
                && drainage != SoilAssessment.Drainage.ANEGADO) {
            crops.add("Zanahoria: la textura arcillosa deforma la raíz y dificulta la cosecha.");
        }
        return crops.stream().limit(6).toList();
    }

    private List<String> cropCare(List<String> recommended, SoilAssessment.Drainage drainage,
                                  SoilAssessment.OrganicMatter organicMatter, boolean compacted) {
        List<String> care = new ArrayList<>();
        for (String crop : recommended) {
            String base = switch (crop) {
                case "Maíz" -> "Maíz: controla malezas los primeros 30 días y ajusta el riego en floración.";
                case "Café" -> "Café: mantén sombra moderada, monitorea roya y conserva la cobertura del suelo.";
                case "Cacao" -> "Cacao: protege del sol directo, controla humedad y revisa mazorcas con frecuencia.";
                case "Tomate" -> "Tomate: tutoriza, ventila el cultivo y evita mojar las hojas al regar.";
                case "Arroz" -> "Arroz: controla la lámina de agua y vigila malezas competitivas.";
                case "Fríjol" -> "Fríjol: rota con gramíneas y evita el exceso de nitrógeno.";
                case "Yuca" -> "Yuca: mantén el suelo suelto y evita encharcamientos prolongados.";
                case "Plátano" -> "Plátano: retira hojas secas, tutoriza y protege del viento fuerte.";
                case "Hortalizas" -> "Hortalizas: usa riego controlado, rotación y cosecha en el punto adecuado.";
                default -> crop + ": observa humedad, malezas y signos de plagas durante todo el ciclo.";
            };
            if (drainage == SoilAssessment.Drainage.RAPIDO) {
                base += " Riega poco y con más frecuencia.";
            } else if (drainage == SoilAssessment.Drainage.LENTO
                    || drainage == SoilAssessment.Drainage.ANEGADO) {
                base += " No riegues hasta comprobar que el suelo drene.";
            }
            if (organicMatter == SoilAssessment.OrganicMatter.BAJA) {
                base += " Aporta compost antes de sembrar.";
            }
            if (compacted) {
                base += " Corrige la compactación antes de la siembra.";
            }
            care.add(base);
        }
        return care.stream().limit(6).toList();
    }

    private List<String> amendments(SoilAssessment.Drainage drainage, SoilAssessment.Slope slope,
                                    SoilAssessment.PhLevel phLevel, SoilAssessment.OrganicMatter organicMatter,
                                    boolean compacted, boolean saline, boolean erosion,
                                    SoilAssessment.SoilColor color) {
        List<String> actions = new ArrayList<>();
        switch (phLevel) {
            case MUY_ACIDO -> actions.add("Aplicar cal agrícola (1 a 2 t/ha), incorporarla y repetir según análisis de laboratorio.");
            case ACIDO -> actions.add("Incorporar cal dolomita para acercar el pH a 6,0.");
            case ALCALINO -> actions.add("Aplicar yeso agrícola y materia orgánica para reducir la alcalinidad.");
            case MUY_ALCALINO -> actions.add("Mejorar el drenaje, aplicar yeso agrícola y evitar fertilizantes alcalinos.");
            default -> {
            }
        }
        switch (organicMatter) {
            case BAJA -> actions.add("Incorporar compost maduro o estiércol descompuesto (3 a 5 t/ha).");
            case MEDIA -> actions.add("Mantener el aporte de materia orgánica con coberturas y compost.");
            case ALTA -> {
            }
        }
        if (compacted) {
            actions.add("Realizar subsolado o labranza profunda para romper la compactación.");
        }
        if (saline) {
            actions.add("Mejorar el drenaje, lavar las sales con riego controlado y evitar fertilizantes con cloro.");
        }
        if (erosion) {
            actions.add("Sembrar coberturas, trazar curvas de nivel y evitar labranza excesiva.");
        }
        switch (drainage) {
            case RAPIDO -> actions.add("Aumentar materia orgánica y usar riego por goteo para retener humedad.");
            case LENTO, ANEGADO -> actions.add("Abrir drenajes y suspender el riego hasta que el suelo drene.");
            case MODERADO -> {
            }
        }
        if (slope == SoilAssessment.Slope.PRONUNCIADO) {
            actions.add("Establecer terrazas o barreras vivas para controlar la escorrentía.");
        }
        if (color == SoilAssessment.SoilColor.CLARO || color == SoilAssessment.SoilColor.GRISACEO) {
            actions.add("Priorizar abonos orgánicos por el bajo contenido de materia orgánica observado.");
        }
        return List.copyOf(actions);
    }

    private List<String> warnings(SoilAssessment.Drainage drainage, SoilAssessment.Slope slope,
                                  SoilAssessment.PhLevel phLevel, SoilAssessment.OrganicMatter organicMatter,
                                  boolean compacted, boolean saline, boolean erosion) {
        List<String> warnings = new ArrayList<>();
        if (phLevel == SoilAssessment.PhLevel.MUY_ACIDO || phLevel == SoilAssessment.PhLevel.MUY_ALCALINO) {
            warnings.add("El pH extremo limita la disponibilidad de nutrientes; confirma el valor con un análisis de laboratorio.");
        }
        if (drainage == SoilAssessment.Drainage.ANEGADO) {
            warnings.add("El encharcamiento puede pudrir raíces; resuelve el drenaje antes de sembrar.");
        }
        if (organicMatter == SoilAssessment.OrganicMatter.BAJA) {
            warnings.add("La materia orgánica baja reduce la fertilidad y la retención de agua.");
        }
        if (compacted) {
            warnings.add("La compactación dificulta el desarrollo de raíces y la infiltración.");
        }
        if (saline) {
            warnings.add("La salinidad detectada requiere análisis de laboratorio antes de sembrar cultivos sensibles.");
        }
        if (erosion) {
            warnings.add("La erosión observada indica pérdida de suelo; aplica prácticas de conservación.");
        }
        if (slope == SoilAssessment.Slope.PRONUNCIADO) {
            warnings.add("La pendiente pronunciada aumenta el riesgo de escorrentía y erosión.");
        }
        return List.copyOf(warnings);
    }

    public record SoilAnalysis(int fertilityScore, String soilType, List<String> recommendedCrops,
                               List<String> discouragedCrops, List<String> cropCare,
                               List<String> amendments, List<String> warnings) {
    }
}
