package ru.servicecompany.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Профиль пользователя.
 * Хранит персональные данные.
 * Пароль и авторизация находятся в auth-service.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    /**
     * Первичный ключ профиля
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * UUID пользователя из auth-service.
     * Это связь между двумя микросервисами.
     */
    @Column(nullable = false, unique = true)
    private UUID authUserId;

    /**
     * Имя
     */
    @Column(nullable = false)
    private String firstName;

    /**
     * Фамилия
     */
    @Column(nullable = false)
    private String lastName;

    /**
     * Отчество
     */
    private String middleName;

    /**
     * Телефон
     */
    @Column(nullable = false)
    private String phone;

    // =====================
    // Адрес проживания
    // =====================

    @Column
    private String city;

    @Column
    private String street;

    @Column
    private String house;

    @Column
    private String apartment;

    @Column
    private UUID zoneId;

    // =====================
    // Фото профиля
    // =====================

    @Lob
    @Column(name = "avatar")
    private byte[] avatar;

    /**
     * Дата создания профиля
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата последнего обновления
     */
    private LocalDateTime updatedAt;

    /**
     * Автоматически вызывается перед INSERT.
     */
    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Автоматически вызывается перед UPDATE.
     */
    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}