package com.wexp.feature.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateUserRequestDto {
    @Size(max = 100, message = "O nome completo não pode exceder 100 caracteres")
    private String username;

    @Size(max = 500, message = "A bio não pode exceder 500 caracteres")
    private String bio;

    @Past(message = "A data de nascimento deve ser uma data no passado")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate birthDate;
}
