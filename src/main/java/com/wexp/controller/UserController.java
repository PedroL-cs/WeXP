package com.wexp.controller;

import com.wexp.database.model.UserEntity;
import com.wexp.dto.UpdateUserRequestDto;
import com.wexp.dto.UserPublicResponseDto;
import com.wexp.dto.UserResponseDto;
import com.wexp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto getCurrentUser(@AuthenticationPrincipal UserEntity currentUser) {
        return userService.getCurrentUser(currentUser);
    }

    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto updateCurrentUser(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestPart(value = "data", required = false) @Valid UpdateUserRequestDto dto,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        return userService.updateCurrentUser(currentUser, dto, avatar);
    }

    @GetMapping("/{publicId}")
    @ResponseStatus(HttpStatus.OK)
    public UserPublicResponseDto getPublicProfile(@PathVariable String publicId) {
        return userService.getPublicProfile(publicId);
    }
}
