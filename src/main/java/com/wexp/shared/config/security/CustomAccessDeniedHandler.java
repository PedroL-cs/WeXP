package com.wexp.shared.config.security;

import com.wexp.shared.dto.ApiErrorResponse;
import com.wexp.shared.exception.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        objectMapper.writeValue(
                response.getOutputStream(),
                ApiErrorResponse.of(
                        ExceptionResponse.Forbidden.getCode(),
                        HttpServletResponse.SC_FORBIDDEN,
                        ExceptionResponse.Forbidden.getMessage(),
                        request.getRequestURI()
                )
        );
    }
}
