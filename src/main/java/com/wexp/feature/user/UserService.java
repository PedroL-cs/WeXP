package com.wexp.feature.user;

import com.wexp.feature.image.ImageService;
import com.wexp.feature.user.dto.UpdateUserRequestDto;
import com.wexp.feature.user.dto.UserPublicResponseDto;
import com.wexp.feature.user.dto.UserResponseDto;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserRepository userRepository;
    private final ImageService imageService;

    public UserResponseDto getCurrentUser(UserEntity user) {
        return new UserResponseDto(user);
    }

    public UserResponseDto updateCurrentUser(UserEntity user, UpdateUserRequestDto dto, MultipartFile avatar) {
        if (dto != null) {
            if (dto.getUsername() != null ) user.setUsername(dto.getUsername());
            if (dto.getBio() != null) user.setBio(dto.getBio());
            if (dto.getBirthDate() != null) user.setBirthDate(dto.getBirthDate());
        }

        if (avatar != null && !avatar.isEmpty()) {
            imageService.replace("user", user.getPublicId(), "profile", avatar);
        }

        UserEntity updatedUser = userRepository.save(user);
        return new UserResponseDto(updatedUser);
    }

    public UserPublicResponseDto getPublicProfile(String publicId) {
        UserEntity user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.UserNotFound));

        return new UserPublicResponseDto(user);
    }
}
