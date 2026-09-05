package com.wexp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wexp.database.model.UserEntity;
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
    private String username;
    private String email;
    private String fullName;
    private String bio;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate birthDate;

    public UserResponseDto(UserEntity user) {
        this.publicId = user.getPublicId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.bio = user.getBio();
        this.birthDate = user.getBirthDate();
    }
}