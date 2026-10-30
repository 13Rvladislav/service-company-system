
package ru.servicecompany.equipment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.servicecompany.equipment.enums.AuditResult;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_equipment_audit_actor_id", columnList = "actor_id"),
                @Index(name = "idx_equipment_audit_entity", columnList = "entity_type, entity_id"),
                @Index(name = "idx_equipment_audit_created_at", columnList = "created_at"),
                @Index(name = "idx_equipment_audit_correlation_id", columnList = "correlation_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "actor_role", nullable = false, length = 30)
    private String actorRole;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 20)
    private AuditResult result;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
