package ru.servicecompany.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.auth.entity.AuditLog;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findAllByActorIdOrderByCreatedAtDesc(UUID actorId);

    List<AuditLog> findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType,
            UUID entityId
    );

    List<AuditLog> findAllByOrderByCreatedAtDesc();
}