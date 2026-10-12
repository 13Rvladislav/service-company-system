
package ru.servicecompany.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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
     * UUID пользователя из auth-service.
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
     * Табельный номер сотрудника.
     */
    @Column(nullable = false, unique = true)
    private String employeeNumber;

    /**
     * Специализации, назначенные мастеру.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "master_profile_specializations",
            joinColumns = @JoinColumn(name = "master_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_master_profile_specialization",
                    columnNames = {"master_profile_id", "specialization_id"}
            )
    )
    private Set<MasterSpecialization> specializations = new HashSet<>();

    /**
     * Закрепленная зона обслуживания.
     */
    private UUID zoneId;

    /**
     * Текущий статус мастера.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MasterStatus status = MasterStatus.AVAILABLE;

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
