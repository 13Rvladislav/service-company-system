
package ru.servicecompany.requestservice.config;

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

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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

                                .requestMatchers(
                                        HttpMethod.OPTIONS,
                                        "/**"
                                ).permitAll()

                                .requestMatchers(
                                        "/swagger",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/actuator/health"
                                ).permitAll()

                                // Создание заявки клиентом
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/requests"
                                ).hasRole("CLIENT")

                                // Список заявок клиента
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/requests/my"
                                ).hasRole("CLIENT")

                                // Редактирование заявки клиентом
                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/requests/**"
                                ).hasRole("CLIENT")

                                // Отмена заявки клиентом
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/requests/*/cancel"
                                ).hasRole("CLIENT")

                                // Назначение или переназначение мастера
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/requests/*/assign"
                                ).hasAnyRole("DISPATCHER", "ADMIN")

                                // Отмена заявки диспетчером или администратором
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/requests/*/admin-cancel"
                                ).hasAnyRole("DISPATCHER", "ADMIN")

                                // Список всех заявок для управления
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/requests/all"
                                ).hasAnyRole("DISPATCHER", "ADMIN")
// Получение кандидатов для ручного назначения мастера
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/requests/*/assignment-candidates"
                                ).hasAnyRole("DISPATCHER", "ADMIN")
                                .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
