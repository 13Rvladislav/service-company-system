package ru.servicecompany.equipment.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // JWT используется вместо сессий
                .csrf(csrf -> csrf.disable())

                .cors(cors -> {})

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // Swagger
                        // =========================
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // =========================
                        // CORS preflight
                        // =========================
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // =========================
                        // Equipment types
                        // =========================

                        // Смотреть типы могут все авторизованные пользователи
                        .requestMatchers(HttpMethod.GET, "/api/equipment/types/**")
                        .hasAnyRole(
                                "CLIENT",
                                "ENGINEER",
                                "DISPATCHER",
                                "ADMIN"
                        )

                        // Создавать типы может только ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/equipment/types/**")
                        .hasRole("ADMIN")

                        // Изменять типы может только ADMIN
                        .requestMatchers(HttpMethod.PUT, "/api/equipment/types/**")
                        .hasRole("ADMIN")

                        // Удалять типы может только ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/equipment/types/**")
                        .hasRole("ADMIN")

                        // =========================
                        // Equipment catalog
                        // =========================

                        // Каталог нужен клиенту для выбора оборудования
                        .requestMatchers(HttpMethod.GET, "/api/equipment/catalog/**")
                        .hasAnyRole(
                                "CLIENT",
                                "ENGINEER",
                                "DISPATCHER",
                                "ADMIN"
                        )

                        // Каталогом управляет только ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/equipment/catalog/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/equipment/catalog/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/equipment/catalog/**")
                        .hasRole("ADMIN")

                        // =========================
                        // My equipment
                        // =========================

                        // Клиент работает только со своим оборудованием
                        .requestMatchers("/api/equipment/my/**")
                        .hasRole("CLIENT")

                        // =========================
                        // Остальные endpoints
                        // =========================
                        .anyRequest().authenticated()
                )

                // JWT достаёт пользователя из HttpOnly cookie
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}