package com.wexp.service;

import com.wexp.database.model.UserEntity;
import com.wexp.database.repository.IUserRepository;
import com.wexp.dto.UserResponseDto;
import com.wexp.exception.ApiException;
import com.wexp.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserRepository userRepository;

    public UserResponseDto getCurrentUser() {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ExceptionResponse.UserNotFound));

        return new UserResponseDto(user);
    }
}
