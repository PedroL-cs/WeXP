package com.wexp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGuideRevisionRequest {
    @NotBlank(message = "O conteúdo modificado não pode estar em branco.")
    private String content;

    @NotBlank(message = "Forneceça uma breve descrição das alterações.")
    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
    private String changeSummary;
}
