package com.agroconecta.infrastructure.adapter.in.web.dto;

import com.agroconecta.application.model.PageResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class CommonDtos {

    private CommonDtos() {
    }

    public record ApiResponse<T>(
            boolean success,
            String message,
            T data,
            Map<String, String> errors,
            Instant timestamp
    ) {

        public static <T> ApiResponse<T> ok(T data) {
            return new ApiResponse<>(true, "Operación realizada correctamente", data, null, Instant.now());
        }

        public static <T> ApiResponse<T> created(T data) {
            return new ApiResponse<>(true, "Recurso creado correctamente", data, null, Instant.now());
        }

        public static <T> ApiResponse<T> message(String message, T data) {
            return new ApiResponse<>(true, message, data, null, Instant.now());
        }

        public static ApiResponse<Void> error(String message) {
            return new ApiResponse<>(false, message, null, null, Instant.now());
        }

        public static ApiResponse<Void> validationError(String message, Map<String, String> errors) {
            return new ApiResponse<>(false, message, null, errors, Instant.now());
        }
    }

    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {

        public static <D, R> PageResponse<R> from(PageResult<D> pageResult, Function<D, R> mapper) {
            List<R> content = pageResult.content().stream().map(mapper).toList();
            return new PageResponse<>(content, pageResult.page(), pageResult.size(),
                    pageResult.totalElements(), pageResult.totalPages());
        }
    }
}
