package com.wexp.feature.image;

import com.wexp.feature.image.dto.ImageResponseDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
@Tag(name = "Image", description = "Gerenciamento genérico de imagens")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "401", description = "Autenticação obrigatória",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse")))
})
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public ImageResponseDto upload(
            @RequestParam String ownerType,
            @RequestParam String ownerId,
            @RequestParam String variant,
            @RequestPart MultipartFile file
    ) {
        return new ImageResponseDto(imageService.replace(ownerType, ownerId, variant, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> get(@PathVariable String id) {
        return imageService.get(id);
    }

    @GetMapping("/{ownerType}/{ownerId}/{variant}")
    public ResponseEntity<Resource> getByOwner(
            @PathVariable String ownerType,
            @PathVariable String ownerId,
            @PathVariable String variant
    ) {
        return imageService.get(ownerType, ownerId, variant);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        imageService.delete(id);
    }
}
