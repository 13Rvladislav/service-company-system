
package ru.servicecompany.requestservice.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.requestservice.config.JwtUserPrincipal;
import ru.servicecompany.requestservice.entity.AuditLog;
import ru.servicecompany.requestservice.enums.AuditResult;
import ru.servicecompany.requestservice.repository.AuditLogRepository;

import java.util.UUID;

/**
 * Единая точка записи аудита в request-service.
 *
 * Сервис присоединяется к текущей транзакции вызывающего
 * бизнес-метода. Отдельная транзакция для аудита не создаётся.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final String SYSTEM_ROLE = "SYSTEM";

    private final AuditLogRepository auditLogRepository;

    /**
     * Записывает успешное действие текущего пользователя.
     *
     * Идентификатор и роль пользователя извлекаются из Spring Security.
     *
     * @param action     код действия
     * @param entityType тип сущности
     * @param entityId   идентификатор сущности
     * @param details    безопасные технические сведения
     */
    @Transactional
    public void logSuccess(
            String action,
            String entityType,
            UUID entityId,
            String details
    ) {
        save(
                action,
                entityType,
                entityId,
                AuditResult.SUCCESS,
                details
        );
    }

    /**
     * Записывает действие, выполненное системой.
     *
     * Используется для фоновых операций и автоматических процессов,
     * у которых нет пользовательского инициатора.
     */
    @Transactional
    public void logSystemSuccess(
            String action,
            String entityType,
            UUID entityId,
            String details
    ) {
        save(
                SYSTEM_ROLE,
                action,
                entityType,
                entityId,
                AuditResult.SUCCESS,
                details
        );
    }

    private void save(
            String action,
            String entityType,
            UUID entityId,
            AuditResult result,
            String details
    ) {
        validateRequired(action, "action");
        validateRequired(entityType, "entityType");

        Actor actor = resolveActor();

        AuditLog auditLog = new AuditLog();

        auditLog.setActorId(actor.userId());
        auditLog.setActorRole(actor.role());
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setResult(result);
        auditLog.setCorrelationId(MDC.get("correlationId"));
        auditLog.setDetails(normalizeDetails(details));

        auditLogRepository.save(auditLog);
    }

    private void save(
            String actorRole,
            String action,
            String entityType,
            UUID entityId,
            AuditResult result,
            String details
    ) {
        validateRequired(action, "action");
        validateRequired(entityType, "entityType");

        AuditLog auditLog = new AuditLog();

        auditLog.setActorId(null);
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
            return new Actor(null, SYSTEM_ROLE);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof JwtUserPrincipal jwtPrincipal) {
            return new Actor(
                    jwtPrincipal.userId(),
                    jwtPrincipal.role()
            );
        }

        return new Actor(null, SYSTEM_ROLE);
    }

    private String normalizeDetails(String details) {
        if (details == null || details.isBlank()) {
            return null;
        }

        String normalized = details.trim();

        if (normalized.length() > 4000) {
            throw new IllegalArgumentException(
                    "Audit details must not exceed 4000 characters"
            );
        }

        return normalized;
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Audit " + fieldName + " must not be blank"
            );
        }
    }

    private record Actor(
            UUID userId,
            String role
    ) {
    }
}
