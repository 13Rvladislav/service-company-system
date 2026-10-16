
package ru.servicecompany.requestservice.config;

import java.util.UUID;

/**
 * Идентификационные данные пользователя,
 * извлечённые из проверенного JWT.
 */
public record JwtUserPrincipal(
        UUID userId,
        String role
) {
}
