
package ru.servicecompany.requestservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.servicecompany.requestservice.entity.AuditLog;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog, UUID> {

    /**
     * История действий конкретного пользователя.
     */
    List<AuditLog> findAllByActorIdOrderByCreatedAtDesc(UUID actorId);

    /**
     * История действий над конкретной сущностью.
     */
    List<AuditLog> findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType,
            UUID entityId
    );
}
