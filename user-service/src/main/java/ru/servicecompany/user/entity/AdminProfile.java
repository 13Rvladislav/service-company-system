package ru.servicecompany.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_profiles")
@Getter
@Setter
@NoArgsConstructor
public class AdminProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * UUID пользователя из auth-service
     */
    @Column(nullable = false, unique = true)
    private UUID authUserId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String middleName;

    @Column(nullable = false)
    private String phone;

    /**
     * Табельный номер администратора
     */
    @Column(nullable = false, unique = true)
    private String employeeNumber;

    /**
     * Должность
     * Например:
     * Главный администратор
     * Системный администратор
     */
    @Column(nullable = false)
    private String position;

    /**
     * Фото профиля
     */
    @Lob
    @Column(name = "avatar")
    private byte[] avatar;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}