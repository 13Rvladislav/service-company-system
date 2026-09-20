package ru.servicecompany.user.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * JWT фильтр.
 * Извлекает токен из HttpOnly Cookie или Bearer Header.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = null;

        // =========================
        // 1. Ищем JWT в Cookie
        // =========================
        if (request.getCookies() != null) {

            for (Cookie cookie : request.getCookies()) {

                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // =========================
        // 2. Если Cookie нет —
        //    ищем Bearer Header
        // =========================
        if (token == null) {

            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
            }
        }

        // Нет токена
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Невалидный токен
        if (!jwtService.isValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // =========================
        // Получаем данные пользователя
        // =========================
        UUID userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        JwtUserPrincipal principal =
                new JwtUserPrincipal(userId, role);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}