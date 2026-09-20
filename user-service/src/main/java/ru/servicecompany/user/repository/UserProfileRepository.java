package ru.servicecompany.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.user.entity.UserProfile;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    /**
     * Поиск профиля по UUID пользователя из auth-service.
     */
    Optional<UserProfile> findByAuthUserId(UUID authUserId);

    /**
     * Проверка существования профиля.
     */
    boolean existsByAuthUserId(UUID authUserId);
}