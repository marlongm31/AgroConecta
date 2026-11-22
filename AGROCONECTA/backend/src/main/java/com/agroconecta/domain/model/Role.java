package com.agroconecta.domain.model;

public enum Role {
    PRODUCTOR,
    COMPRADOR,
    ADMINISTRADOR;

    public boolean isSelfAssignable() {
        return this == PRODUCTOR || this == COMPRADOR;
    }
}
