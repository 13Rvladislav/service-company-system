package ru.servicecompany.equipment.config;

import java.util.UUID;

/**
 * Пользователь, извлечённый из JWT.
 */
public record JwtUserPrincipal(
        UUID userId,
        String role
) {
}