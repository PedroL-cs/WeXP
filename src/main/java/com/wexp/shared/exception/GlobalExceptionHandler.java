package com.wexp.shared.exception;

import com.wexp.shared.dto.ApiErrorResponse;
import com.wexp.shared.dto.ApiFieldError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.wexp.shared.pagination.InvalidSortException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(
            ApiException exception,
            HttpServletRequest request
    ) {
        return exception.getExceptionResponse().toResponseEntity(request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            HttpServletRequest request
    ) {
        return response(
                ExceptionResponse.InvalidCredentials,
                request,
                ExceptionResponse.InvalidCredentials.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ApiFieldError> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::fieldError)
                .toList();
        return ResponseEntity.badRequest().body(ApiErrorResponse.validation(
                ExceptionResponse.InvalidInput.getCode(),
                request.getRequestURI(),
                errors
        ));
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MissingPathVariableException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            Exception exception,
            HttpServletRequest request
    ) {
        return response(ExceptionResponse.InvalidInput, request, clientMessage(exception));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidArgument(
            HttpServletRequest request
    ) {
        return response(ExceptionResponse.InvalidInput, request, "Um parâmetro da requisição possui um valor inválido");
    }

    @ExceptionHandler(InvalidSortException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidSort(
            InvalidSortException exception,
            HttpServletRequest request
    ) {
        String allowed = String.join(", ", exception.getAllowedProperties());
        return response(
                ExceptionResponse.InvalidInput,
                request,
                "A ordenação '" + exception.getProperty()
                        + "' é inválida. Utilize uma propriedade entre: " + allowed
                        + ". Formato esperado: campo,asc ou campo,desc"
        );
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorResponse.of(
                ExceptionResponse.RouteNotFound.getCode(),
                HttpStatus.NOT_FOUND.value(),
                ExceptionResponse.RouteNotFound.getMessage(),
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiErrorResponse.of(
                ExceptionResponse.MethodNotAllowed.getCode(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                ExceptionResponse.MethodNotAllowed.getMessage(),
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        logger.error("Erro inesperado na API em {}", request.getRequestURI(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiErrorResponse.of(
                ExceptionResponse.InternalServerError.getCode(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro inesperado no servidor",
                request.getRequestURI()
        ));
    }

    private ApiFieldError fieldError(FieldError error) {
        return new ApiFieldError(error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<ApiErrorResponse> response(
            ExceptionResponse exceptionResponse,
            HttpServletRequest request,
            String message
    ) {
        return ResponseEntity.status(exceptionResponse.getHttpStatus()).body(ApiErrorResponse.of(
                exceptionResponse.getCode(),
                exceptionResponse.getHttpStatus().value(),
                message,
                request.getRequestURI()
        ));
    }

    private String clientMessage(Exception exception) {
        if (exception instanceof MissingServletRequestParameterException missing) {
            return "O parâmetro obrigatório '" + missing.getParameterName() + "' não foi informado";
        }
        if (exception instanceof MissingServletRequestPartException missing) {
            return "A parte obrigatória '" + missing.getRequestPartName() + "' não foi informada";
        }
        if (exception instanceof MissingPathVariableException missing) {
            return "A variável de caminho obrigatória '" + missing.getVariableName() + "' não foi informada";
        }
        if (exception instanceof MethodArgumentTypeMismatchException mismatch) {
            return "O parâmetro '" + mismatch.getName() + "' possui um valor inválido";
        }
        if (exception instanceof HttpMediaTypeNotSupportedException) {
            return "O tipo de conteúdo não é suportado";
        }
        return "O corpo da requisição é inválido";
    }
}
