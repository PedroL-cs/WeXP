package com.wexp.shared.dto;

public record ApiFieldError(
        String field,
        String message
) {
}
