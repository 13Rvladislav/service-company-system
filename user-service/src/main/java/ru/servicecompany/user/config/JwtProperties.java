package ru.servicecompany.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Настройки JWT, считываются из application.yml.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Секретный ключ подписи JWT.
     */
    private String secret;

    /**
     * Время жизни токена (мс).
     */
    private Long expiration;

}