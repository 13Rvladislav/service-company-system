
package ru.servicecompany.requestservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Проверяет подпись и срок действия токена.
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Получает идентификатор и роль пользователя из JWT.
     */
    public JwtUserPrincipal extractPrincipal(String token) {
        Claims claims = extractClaims(token);

        String subject = claims.getSubject();
        String role = claims.get("role", String.class);

        if (subject == null || role == null || role.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT не содержит идентификатор пользователя или роль"
            );
        }

        return new JwtUserPrincipal(
                UUID.fromString(subject),
                role
        );
    }
}
