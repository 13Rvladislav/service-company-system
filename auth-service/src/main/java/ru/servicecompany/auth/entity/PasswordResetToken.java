package ru.servicecompany.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Одноразовый токен для восстановления пароля.
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    /**
     * Первичный ключ.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Сам токен, который отправится пользователю.
     */
    @Column(nullable = false, unique = true)
    private String token;

    /**
     * Пользователь, которому принадлежит токен.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Время окончания действия токена.
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Был ли токен уже использован.
     */
    @Column(nullable = false)
    private Boolean used;

    /**
     * Дата создания записи.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

}