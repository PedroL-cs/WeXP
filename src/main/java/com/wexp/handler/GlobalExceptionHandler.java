package com.wexp.handler;

import com.wexp.exception.ApiException;
import com.wexp.exception.ExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(ApiException e) {
        return e.getExceptionResponse().toResponseEntity();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception e) {
        logger.error(e.getMessage());
        return ExceptionResponse.InternalServerError.toResponseEntity();
    }
}
