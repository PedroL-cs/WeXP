package com.wexp.dto;

import com.wexp.database.model.UserEntity;
import com.wexp.service.ImageService;
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
        this.avatar = ImageService.getAvatarUrl(user.getPublicId());
    }
}
