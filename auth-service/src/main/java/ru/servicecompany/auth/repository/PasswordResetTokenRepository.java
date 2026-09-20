package ru.servicecompany.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.auth.entity.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Поиск токена восстановления.
     */
    Optional<PasswordResetToken> findByToken(String token);

}