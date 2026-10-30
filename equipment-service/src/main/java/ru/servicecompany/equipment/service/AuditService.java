
package ru.servicecompany.equipment.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.equipment.config.JwtUserPrincipal;
import ru.servicecompany.equipment.entity.AuditLog;
import ru.servicecompany.equipment.enums.AuditResult;
import ru.servicecompany.equipment.repository.AuditLogRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final int MAX_DETAILS_LENGTH = 4000;

    private final AuditLogRepository auditLogRepository;

    /**
     * Записать успешное действие текущего пользователя.
     */
    @Transactional
    public void logSuccess(
            String action,
            String entityType,
            UUID entityId,
            String details
    ) {
        Actor actor = resolveActor();

        save(
                actor.userId(),
                actor.role(),
                action,
                entityType,
                entityId,
                AuditResult.SUCCESS,
                details
        );
    }

    /**
     * Записать системное действие.
     */
    @Transactional
    public void logSystemSuccess(
            String action,
            String entityType,
            UUID entityId,
            String details
    ) {
        save(
                null,
                "SYSTEM",
                action,
                entityType,
                entityId,
                AuditResult.SUCCESS,
                details
        );
    }

    private void save(
            UUID actorId,
            String actorRole,
            String action,
            String entityType,
            UUID entityId,
            AuditResult result,
            String details
    ) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException(
                    "Audit action must not be blank"
            );
        }

        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException(
                    "Audit entityType must not be blank"
            );
        }

        AuditLog auditLog = new AuditLog();

        auditLog.setActorId(actorId);
        auditLog.setActorRole(actorRole);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setResult(result);
        auditLog.setCorrelationId(MDC.get("correlationId"));
        auditLog.setDetails(normalizeDetails(details));

        auditLogRepository.save(auditLog);
    }

    private Actor resolveActor() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return new Actor(null, "SYSTEM");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof JwtUserPrincipal jwtPrincipal) {
            return new Actor(
                    jwtPrincipal.userId(),
                    jwtPrincipal.role()
            );
        }

        return new Actor(null, "SYSTEM");
    }

    private String normalizeDetails(String details) {

        if (details == null || details.isBlank()) {
            return null;
        }

        String normalized = details.trim();

        if (normalized.length() > MAX_DETAILS_LENGTH) {
            throw new IllegalArgumentException(
                    "Audit details must not exceed 4000 characters"
            );
        }

        return normalized;
    }

    private record Actor(
            UUID userId,
            String role
    ) {
    }
}
