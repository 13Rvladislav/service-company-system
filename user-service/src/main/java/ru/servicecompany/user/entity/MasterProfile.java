package ru.servicecompany.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "master_profiles")
@Getter
@Setter
@NoArgsConstructor
public class MasterProfile {

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
     * Табельный номер сотрудника
     */
    @Column(nullable = false, unique = true)
    private String employeeNumber;

    /**
     * Специализация мастера
     * (котлы, колонки и т.д.)
     */
    @Column(nullable = false)
    private String specialization;

    /**
     * Закрепленная зона обслуживания
     */
    private UUID zoneId;

    /**
     * Текущий статус мастера
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MasterStatus status = MasterStatus.AVAILABLE;

    // =====================
    // Фото профиля
    // =====================

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