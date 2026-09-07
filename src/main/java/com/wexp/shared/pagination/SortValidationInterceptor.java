package com.wexp.shared.pagination;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;

@Component
public class SortValidationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        AllowedSort annotation = handlerMethod.getMethodAnnotation(AllowedSort.class);
        if (annotation == null) {
            return true;
        }

        Set<String> allowedProperties = Set.of(annotation.value());
        validatePagination(request);
        String[] sortParameters = request.getParameterValues("sort");
        if (sortParameters == null) {
            return true;
        }

        Arrays.stream(sortParameters)
                .flatMap(sort -> Arrays.stream(sort.split(",")))
                .findFirst()
                .ifPresent(sort -> validateSortParameters(sortParameters, allowedProperties));

        return true;
    }

    private void validatePagination(HttpServletRequest request) {
        String page = request.getParameter("page");
        String size = request.getParameter("size");
        try {
            if (page != null && Integer.parseInt(page) < 0) {
                throw new IllegalArgumentException("Página inválida");
            }
            if (size != null && Integer.parseInt(size) <= 0) {
                throw new IllegalArgumentException("Tamanho inválido");
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Paginação inválida", exception);
        }
    }

    private void validateSortParameters(String[] sortParameters, Set<String> allowedProperties) {
        for (String sortParameter : sortParameters) {
            String[] parts = sortParameter.split(",", -1);
            String property = parts[0].trim();

            if (property.isBlank() || parts.length > 2) {
                throw new InvalidSortException(sortParameter, allowedProperties);
            }

            if (parts.length == 2
                    && !("asc".equalsIgnoreCase(parts[1].trim())
                    || "desc".equalsIgnoreCase(parts[1].trim()))) {
                throw new InvalidSortException(sortParameter, allowedProperties);
            }

            if (!allowedProperties.contains(property)) {
                throw new InvalidSortException(property, allowedProperties);
            }
        }
    }
}
