package com.wexp.feature.revision.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGuideRevisionRequest {
    @NotBlank(message = "Informe a versão utilizada como base para a revisão.")
    @Size(max = 16)
    private String baseRevisionId;

    @NotBlank(message = "O conteúdo modificado não pode estar em branco.")
    @Size(min = 10, max = 100000, message = "O conteúdo deve ter entre 10 e 100000 caracteres.")
    private String content;

    @NotBlank(message = "Forneça uma breve descrição das alterações.")
    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
    private String changeSummary;
}
