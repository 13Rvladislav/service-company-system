
package ru.servicecompany.requestservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.servicecompany.requestservice.enums.RequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Заявка клиента на обслуживание оборудования.
 */
@Entity
@Table(name = "service_requests")
@Getter
@Setter
@NoArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Идентификатор клиента из user-service.
     * Не используем JPA-связь между разными микросервисами.
     */
    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    /**
     * Идентификатор назначенного мастера.
     * null, пока мастер не назначен.
     */
    @Column(name = "master_id")
    private UUID masterId;

    /**
     * Идентификатор адреса обслуживания.
     */
    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    /**
     * Идентификатор оборудования.
     */
    @Column(name = "equipment_id")
    private UUID equipmentId;

    /**
     * Описание проблемы со слов клиента.
     */
    @Column(name = "problem_description", nullable = false, columnDefinition = "TEXT")
    private String problemDescription;

    /**
     * Дополнительный комментарий клиента.
     */
    @Column(name = "client_comment", columnDefinition = "TEXT")
    private String clientComment;

    /**
     * Текущий статус заявки.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private RequestStatus status;

    /**
     * Желаемое время визита.
     */
    @Column(name = "preferred_visit_at")
    private LocalDateTime preferredVisitAt;

    /**
     * Дата создания заявки.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата последнего изменения заявки.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = RequestStatus.NEW;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
