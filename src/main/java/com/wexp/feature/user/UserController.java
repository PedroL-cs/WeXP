package com.wexp.feature.user;

import com.wexp.feature.user.dto.UpdateUserRequestDto;
import com.wexp.feature.user.dto.UserPublicResponseDto;
import com.wexp.feature.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "Perfis de usuário")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "401", description = "Autenticação obrigatória",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse")))
})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto getCurrentUser(@AuthenticationPrincipal UserEntity currentUser) {
        return userService.getCurrentUser(currentUser);
    }

    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto updateCurrentUser(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestPart(value = "data", required = false) @Valid UpdateUserRequestDto dto,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        return userService.updateCurrentUser(currentUser, dto, avatar);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserPublicResponseDto getPublicProfile(@PathVariable String id) {
        return userService.getPublicProfile(id);
    }
}
