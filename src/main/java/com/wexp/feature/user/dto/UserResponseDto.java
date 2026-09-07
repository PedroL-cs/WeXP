package com.wexp.feature.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wexp.feature.user.UserEntity;
import com.wexp.feature.image.ImageService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private String publicId;
    private String login;
    private String email;
    private String username;
    private String bio;
    private String avatar;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate birthDate;

    public UserResponseDto(UserEntity user) {
        this.publicId = user.getPublicId();
        this.login = user.getUsername();
        this.email = user.getEmail();
        this.username= user.getUsername();
        this.bio = user.getBio();
        this.birthDate = user.getBirthDate();
        this.avatar = ImageService.url("user", user.getPublicId(), "profile");
    }
}