package com.wexp.feature.user.dto;

import com.wexp.feature.user.UserEntity;
import com.wexp.feature.image.ImageService;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPublicResponseDto {
    private String publicId;
    private String username;
    private String bio;
    private String avatar;

    public UserPublicResponseDto(UserEntity user) {
        this.publicId = user.getPublicId();
        this.username = user.getUsername();
        this.bio = user.getBio();
        this.avatar = ImageService.url("user", user.getPublicId(), "profile");
    }
}
