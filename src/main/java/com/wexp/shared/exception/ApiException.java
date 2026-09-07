package com.wexp.shared.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ExceptionResponse exceptionResponse;

    public ApiException(ExceptionResponse exceptionMessage) {
        super(exceptionMessage.getMessage());
        this.exceptionResponse = exceptionMessage;
    }
}
