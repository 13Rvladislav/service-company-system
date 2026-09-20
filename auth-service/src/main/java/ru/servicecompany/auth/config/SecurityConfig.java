package ru.servicecompany.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.servicecompany.auth.security.JwtAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // Наш JWT фильтр, который будет проверять токен
    private final JwtAuthenticationFilter jwtFilter;

    /**
     * Главная конфигурация безопасности приложения.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                // Отключаем CSRF, потому что используем REST + JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Сервер не хранит HTTP-сессии
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Настраиваем доступ к URL
                .authorizeHttpRequests(auth -> auth

                        // Эти маршруты доступны без авторизации
                        .requestMatchers("/api/auth/**").permitAll()

                        // Swagger тоже открыт
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger"
                        ).permitAll()

                        // Всё остальное только после JWT
                        .anyRequest().authenticated()
                )

                /*
                 * Добавляем наш фильтр ПЕРЕД стандартной
                 * авторизацией Spring Security.
                 */
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // Пока оставим стандартную конфигурацию
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * BCrypt используется для регистрации и проверки пароля.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}