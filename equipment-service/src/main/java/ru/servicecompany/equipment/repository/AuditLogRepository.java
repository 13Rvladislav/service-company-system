
package ru.servicecompany.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.servicecompany.equipment.entity.AuditLog;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findAllByActorIdOrderByCreatedAtDesc(
            UUID actorId
    );

    List<AuditLog> findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType,
            UUID entityId
    );

    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
