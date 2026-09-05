package com.wexp.dto;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private final String token;
    private final String type = "Bearer";

    public LoginResponseDto(String token) {
        this.token = token;
    }
}