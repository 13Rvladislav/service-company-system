package ru.servicecompany.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * Этот класс автоматически считывает значения
 * из application.yml с префиксом jwt.
 *
 * jwt.secret
 * jwt.expiration
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    // Секретный ключ для подписи токена
    private String secret;

    // Время жизни токена в миллисекундах
    private Long expiration;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
}