package ru.servicecompany.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import ru.servicecompany.auth.config.JwtProperties;
import ru.servicecompany.auth.entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * Создаём ключ подписи из secret.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Генерация JWT.
     */
    public String generateToken(User user) {

        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                // UUID пользователя
                .subject(user.getId().toString())

                // дополнительные данные
                .claim("email", user.getEmail())
                .claim("role", user.getRole().getName().name())

                .issuer("service-company-system")
                .issuedAt(now)
                .expiration(expiration)

                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Получаем все данные из токена.
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Получаем email пользователя.
     */
    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    /**
     * Проверяем, что токен не просрочен.
     */
    public boolean isTokenValid(String token) {

        Date expiration = extractAllClaims(token).getExpiration();

        return expiration.after(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}