package com.wexp.service;

import com.wexp.database.model.UserEntity;
import com.wexp.database.repository.IUserRepository;
import com.wexp.dto.LoginRequestDto;
import com.wexp.dto.LoginResponseDto;
import com.wexp.dto.RegisterRequestDto;
import com.wexp.exception.ApiException;
import com.wexp.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public LoginResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getLogin(),
                        request.getPassword()
                )
        );

        UserEntity user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new ApiException(ExceptionResponse.UserNotFound));

        String token = jwtService.generateToken(user);
        return new LoginResponseDto(token);
    }

    public LoginResponseDto register(RegisterRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ApiException(ExceptionResponse.EmailAlreadyInUse);
        }

        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            throw new ApiException(ExceptionResponse.UsernameAlreadyInUse);
        }

        UserEntity user = UserEntity.builder()
                .login(request.getLogin())
                .email(request.getEmail())
                .username(request.getUsername())
                .birthDate(request.getBirthDate())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new LoginResponseDto(token);
    }
}