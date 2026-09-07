package com.wexp.service;

import com.wexp.database.model.UserEntity;
import com.wexp.database.repository.IUserRepository;
import com.wexp.dto.UpdateUserRequestDto;
import com.wexp.dto.UserPublicResponseDto;
import com.wexp.dto.UserResponseDto;
import com.wexp.exception.ApiException;
import com.wexp.exception.ExceptionResponse;
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
            imageService.saveUserAvatar(user.getPublicId(), avatar);
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
