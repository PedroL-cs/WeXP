package com.wexp.feature.auth;

import com.wexp.feature.auth.dto.LoginRequestDto;
import com.wexp.feature.auth.dto.LoginResponseDto;
import com.wexp.feature.auth.dto.RegisterRequestDto;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Autenticação e cadastro")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "409", description = "Conflito de recurso",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse")))
})
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }
}