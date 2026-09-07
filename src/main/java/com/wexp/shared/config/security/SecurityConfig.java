package com.wexp.shared.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // Rotas públicas
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/games/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/genres/**").permitAll()
                        .requestMatchers("/api/v1/achievements/*/guide/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/achievements/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/images/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Rotas protegidas
                        .requestMatchers("/api/v1/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/*").permitAll()
                        .requestMatchers("/api/v1/guides/**").authenticated()
                        // Todas as outras rotas
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}