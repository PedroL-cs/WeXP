package com.wexp.feature.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByPublicId(String username);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByLogin(String login);
}
