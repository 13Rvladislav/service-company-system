
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

                        // Actuator: мониторинг
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/prometheus"
                        ).permitAll()

                        .requestMatchers(
                                "/swagger",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Аналитика.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/analytics"
                        ).hasAnyRole("DISPATCHER", "ADMIN")

                        // Создание заявки клиентом.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/requests"
                        ).hasRole("CLIENT")

                        // Список заявок клиента.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/requests/my"
                        ).hasRole("CLIENT")

                        // Список назначенных заявок мастера.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/requests/master"
                        ).hasRole("ENGINEER")

                        // Принятие заявки мастером.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/requests/*/master-accept"
                        ).hasRole("ENGINEER")

                        // Отклонение назначения мастером.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/requests/*/master-reject"
                        ).hasRole("ENGINEER")

                        // Редактирование заявки клиентом.
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/requests/**"
                        ).hasRole("CLIENT")

                        // Отмена заявки клиентом.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/requests/*/cancel"
                        ).hasRole("CLIENT")

                        // Ручное назначение мастера.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/requests/*/assign"
                        ).hasAnyRole("DISPATCHER", "ADMIN")

                        // Отмена заявки диспетчером или администратором.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/requests/*/admin-cancel"
                        ).hasAnyRole("DISPATCHER", "ADMIN")

                        // Список всех заявок для управления.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/requests/all"
                        ).hasAnyRole("DISPATCHER", "ADMIN")

                        // Кандидаты для назначения мастера.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/requests/*/assignment-candidates"
                        ).hasAnyRole("DISPATCHER", "ADMIN")

                        // Просмотр отчёта.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/requests/*/report"
                        ).hasAnyRole(
                                "CLIENT",
                                "ENGINEER",
                                "DISPATCHER",
                                "ADMIN"
                        )

                        // Скачивание вложения отчёта.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/requests/*/report/attachments/*"
                        ).hasAnyRole(
                                "CLIENT",
                                "ENGINEER",
                                "DISPATCHER",
                                "ADMIN"
                        )

                        // Сохранение черновика мастером.
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/requests/*/report"
                        ).hasRole("ENGINEER")

                        // Отправка отчёта мастером.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/requests/*/report/submit"
                        ).hasRole("ENGINEER")

                        // Загрузка вложения мастером.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/requests/*/report/attachments"
                        ).hasRole("ENGINEER")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
