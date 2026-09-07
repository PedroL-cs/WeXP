package com.wexp.shared.pagination;

import lombok.Getter;

import java.util.Set;

@Getter
public class InvalidSortException extends RuntimeException {

    private final String property;
    private final Set<String> allowedProperties;

    public InvalidSortException(String property, Set<String> allowedProperties) {
        this.property = property;
        this.allowedProperties = allowedProperties;
    }

}
