
package ru.servicecompany.requestservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.servicecompany.requestservice.enums.AuditResult;

import java.time.Instant;
import java.util.UUID;

/**
 * Запись аудита действий пользователей.
 *
 * Хранит техническую информацию о том, кто выполнил действие,
 * над какой сущностью и с каким результатом.
 *
 * Персональные данные и секреты в эту сущность не записываются.
 */
@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_logs_actor_id", columnList = "actor_id"),
                @Index(name = "idx_audit_logs_entity", columnList = "entity_type, entity_id"),
                @Index(name = "idx_audit_logs_created_at", columnList = "created_at"),
                @Index(name = "idx_audit_logs_correlation_id", columnList = "correlation_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * UUID пользователя, выполнившего действие.
     * Для системных операций может быть null.
     */
    @Column(name = "actor_id")
    private UUID actorId;

    /**
     * Роль пользователя: CLIENT, ENGINEER, DISPATCHER, ADMIN.
     * Для системных операций используется SYSTEM.
     */
    @Column(name = "actor_role", nullable = false, length = 30)
    private String actorRole;

    /**
     * Код выполненного действия.
     * Например: REQUEST_CREATED, MASTER_ASSIGNED.
     */
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    /**
     * Тип сущности, над которой выполнено действие.
     * Например: SERVICE_REQUEST, WORK_REPORT.
     */
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    /**
     * UUID сущности.
     */
    @Column(name = "entity_id")
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 20)
    private AuditResult result;

    /**
     * Идентификатор исходного HTTP-запроса.
     */
    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    /**
     * Дополнительные безопасные технические сведения.
     *
     * Запрещено сохранять здесь пароли, токены, персональные данные,
     * адреса, телефоны, email и содержимое отчётов.
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @jakarta.persistence.PrePersist
    protected void prePersist() {
        onCreate();
    }
}
