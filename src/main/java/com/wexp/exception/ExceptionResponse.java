package com.wexp.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExceptionResponse {

    InternalServerError(0, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    GameNotFound(1, "Jogo não encontrado", HttpStatus.NOT_FOUND),
    SteamGameNotFound(2, "Jogo da steam não encontrado", HttpStatus.NOT_FOUND),
    ImageNotFound(3, "Image não encontrada", HttpStatus.NOT_FOUND),
    CategoryNotFound(4, "Categoria não encontrada", HttpStatus.NOT_FOUND),
    GenreNotFound(5, "Género não encontrado", HttpStatus.NOT_FOUND),
    AchievementNotFound(6, "Conquista não encontrada", HttpStatus.NOT_FOUND),
    UserNotFound(7, "Usuário não encontrado", HttpStatus.NOT_FOUND),
    EmailAlreadyInUse(8, "O email já está sendo utilizado", HttpStatus.CONFLICT),
    UsernameAlreadyInUse(9, "O nome de usuário já está sendo utilizado", HttpStatus.CONFLICT),
    InvalidCredentials(10, "Usuário ou senha inválidos", HttpStatus.UNAUTHORIZED),
    Unauthorized(11, "Acesso não autorizado. Faça login para continuar", HttpStatus.UNAUTHORIZED),
    InvalidInput(12, "Dados de entrada inválidos", HttpStatus.BAD_REQUEST),
    GuideNotFound(13, "Guia não encontrado", HttpStatus.NOT_FOUND),
    GuideAlreadyExists(14, "Esta conquista já possui um guia oficial", HttpStatus.CONFLICT);

    private final Integer code;
    private final String message;
    @JsonIgnore private final HttpStatus httpStatus;

    public ResponseEntity<ExceptionResponse> toResponseEntity() {
        return ResponseEntity.status(httpStatus).body(this);
    }
}
