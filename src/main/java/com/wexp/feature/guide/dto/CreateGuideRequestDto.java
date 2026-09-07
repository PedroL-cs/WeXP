package com.wexp.feature.guide.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGuideRequestDto {

    @NotBlank(message = "O conteúdo do guia é obrigatório")
    @Size(min = 10, message = "O guia deve ter no mínimo 10 caracteres")
    private String content;
}
