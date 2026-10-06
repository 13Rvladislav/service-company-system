package ru.servicecompany.equipment.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // JWT используется вместо CSRF-защиты
                .csrf(csrf -> csrf.disable())

                // Разрешаем CORS
                .cors(cors -> {})

                // Сервис работает stateless.
                // Сервер не хранит HTTP-сессии пользователей.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // ==========================
                        // Swagger / OpenAPI
                        // ==========================

                        .requestMatchers(
                                "/swagger",
                                "/swagger/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()

                        // ==========================
                        // Справочник типов оборудования
                        // ==========================

                        .requestMatchers(
                                "/api/equipment/types/**"
                        ).hasRole("ADMIN")

                        // ==========================
                        // Справочник оборудования
                        // ==========================

                        .requestMatchers(
                                "/api/equipment/catalog/**"
                        ).hasRole("ADMIN")

                        // ==========================
                        // Оборудование текущего клиента
                        // Пока контроллеров нет,
                        // но правило закладываем заранее.
                        // ==========================

                        .requestMatchers(
                                "/api/equipment/my/**"
                        ).hasRole("CLIENT")

                        // ==========================
                        // Все остальные запросы
                        // ==========================

                        .anyRequest().authenticated()
                )

                // Наш JWT-фильтр должен выполняться
                // до стандартного UsernamePasswordAuthenticationFilter.
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}