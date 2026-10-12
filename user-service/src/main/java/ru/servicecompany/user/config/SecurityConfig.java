
package ru.servicecompany.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // CORS preflight
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Внутренние endpoint'ы микросервисов
                        .requestMatchers("/internal/**")
                        .permitAll()

                        // Создание специализации
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/specializations"
                        ).hasRole("ADMIN")

                        // Редактирование специализации
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/specializations/**"
                        ).hasRole("ADMIN")

                        // Удаление специализации
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/specializations/**"
                        ).hasRole("ADMIN")

                        // Просмотр справочника специализаций
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/specializations/**"
                        ).authenticated()

                        // Просмотр назначенных специализаций мастера
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/masters/*/specializations"
                        ).hasAnyRole("ADMIN", "DISPATCHER")

                        // Назначение специализаций мастеру
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/masters/*/specializations"
                        ).hasAnyRole("ADMIN", "DISPATCHER")

                        // Текущий профиль пользователя
                        .requestMatchers("/api/profile/me")
                        .authenticated()

                        // Все остальные endpoint'ы
                        .anyRequest()
                        .authenticated()
                )

                .httpBasic(AbstractHttpConfigurer::disable)

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
