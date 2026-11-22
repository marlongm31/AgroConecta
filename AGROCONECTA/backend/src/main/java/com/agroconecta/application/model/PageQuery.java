package com.agroconecta.application.model;

public record PageQuery(int page, int size) {

    public PageQuery {
        page = Math.max(page, 0);
        size = Math.min(Math.max(size, 1), 100);
    }

    public static PageQuery of(int page, int size) {
        return new PageQuery(page, size);
    }
}
