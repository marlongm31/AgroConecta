package com.agroconecta.domain.exception;

public class NotFoundException extends DomainException {

    public NotFoundException(String resource, Object id) {
        super(resource + " no encontrado(a) con id " + id);
    }

    public NotFoundException(String message) {
        super(message);
    }
}
