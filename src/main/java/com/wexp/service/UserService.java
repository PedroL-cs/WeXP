package com.wexp.service;

import com.wexp.database.model.UserEntity;
import com.wexp.database.repository.IUserRepository;
import com.wexp.dto.UpdateUserRequestDto;
import com.wexp.dto.UserResponseDto;
import com.wexp.exception.ApiException;
import com.wexp.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserRepository userRepository;
    private final ImageService imageService;

    public UserResponseDto getCurrentUser() {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ExceptionResponse.UserNotFound));

        return new UserResponseDto(user);
    }

    public UserResponseDto updateCurrentUser(UpdateUserRequestDto dto, MultipartFile avatar) {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ExceptionResponse.UserNotFound));

        if (dto != null) {
            if (dto.getFullName() != null ) user.setFullName(dto.getFullName());
            if (dto.getBio() != null) user.setBio(dto.getBio());
            if (dto.getBirthDate() != null) user.setBirthDate(dto.getBirthDate());
        }

        if (avatar != null && !avatar.isEmpty()) {
            imageService.saveUserAvatar(user.getPublicId(), avatar);
        }

        UserEntity updatedUser = userRepository.save(user);
        return new UserResponseDto(updatedUser);
    }
}
