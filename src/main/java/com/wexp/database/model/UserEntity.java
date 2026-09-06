package com.wexp.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wexp.utils.PublicIdGenerator;
import com.wexp.utils.PublicIdType;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity implements UserDetails {

    @JsonIgnore
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("id")
    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 13)
    private String publicId;

    @Column(nullable = false, unique = true, updatable = false, length = 30)
    private String login;

    @Column(nullable = false, unique = true, updatable = false, length = 256)
    private String email;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 256)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.publicId == null) {
            this.publicId = PublicIdGenerator.generate(PublicIdType.USER);
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    @NullMarked
    public String getUsername() {
        return this.login;
    }

    @Override
    @NullMarked
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @NullMarked
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @NullMarked
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @NullMarked
    public boolean isEnabled() {
        return true;
    }
}