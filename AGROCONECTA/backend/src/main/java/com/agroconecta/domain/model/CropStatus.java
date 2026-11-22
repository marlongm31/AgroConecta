package com.agroconecta.domain.model;

public enum CropStatus {
    PLANIFICADO,
    EN_CRECIMIENTO,
    LISTO_PARA_COSECHA,
    COSECHADO,
    PERDIDO;

    public boolean isActive() {
        return this == PLANIFICADO || this == EN_CRECIMIENTO || this == LISTO_PARA_COSECHA;
    }
}
