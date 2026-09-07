package com.wexp.feature.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    @NotBlank(message = "O login é obrigatório")
    private String login;

    @NotBlank(message = "A senha é obrigatória")
    private String password;
}