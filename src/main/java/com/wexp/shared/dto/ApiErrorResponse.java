package com.wexp.shared.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        Integer code,
        Integer status,
        String message,
        LocalDateTime timestamp,
        String path,
        List<ApiFieldError> errors
) {
    public ApiErrorResponse(Integer code, String message) {
        this(code, null, message, null, null, List.of());
    }

    public static ApiErrorResponse of(
            Integer code,
            Integer status,
            String message,
            String path
    ) {
        return new ApiErrorResponse(code, status, message, LocalDateTime.now(), path, List.of());
    }

    public static ApiErrorResponse validation(
            Integer code,
            String path,
            List<ApiFieldError> errors
    ) {
        return new ApiErrorResponse(
                code,
                400,
                "A validação da requisição falhou",
                LocalDateTime.now(),
                path,
                errors
        );
    }
}
