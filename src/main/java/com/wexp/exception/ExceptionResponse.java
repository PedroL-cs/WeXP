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
    GenreNotFound(5, "Genero não encontrado", HttpStatus.NOT_FOUND),;

    private final Integer code;
    private final String message;
    @JsonIgnore private final HttpStatus httpStatus;

    public ResponseEntity<ExceptionResponse> toResponseEntity() {
        return ResponseEntity.status(httpStatus).body(this);
    }
}
