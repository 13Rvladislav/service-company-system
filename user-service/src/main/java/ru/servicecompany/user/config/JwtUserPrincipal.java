package ru.servicecompany.user.config;

import java.util.UUID;

/**
 * Пользователь, извлечённый из JWT.
 */
public record JwtUserPrincipal(
        UUID userId,
        String role
) {}