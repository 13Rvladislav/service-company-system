
package ru.servicecompany.user.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.user.config.JwtUserPrincipal;
import ru.servicecompany.user.entity.AuditLog;
import ru.servicecompany.user.enums.AuditResult;
import ru.servicecompany.user.repository.AuditLogRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final int MAX_DETAILS_LENGTH = 4000;

    private final AuditLogRepository auditLogRepository;

    /**
     * Записать успешное действие текущего пользователя.
     *
     * Используется внутри бизнес-транзакции.
     * По умолчанию REQUIRED: запись аудита участвует
     * в транзакции вызывающего сервиса.
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
                details
        );
    }

    /**
     * Записать системное действие.
     *
     * Например, автоматическое изменение состояния
     * или выполнение фоновой операции.
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
                details
        );
    }

    private void save(
            UUID actorId,
            String actorRole,
            String action,
            String entityType,
            UUID entityId,
            String details
    ) {
        AuditLog auditLog = new AuditLog();

        auditLog.setActorId(actorId);
        auditLog.setActorRole(actorRole);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setResult(AuditResult.SUCCESS);
        auditLog.setCorrelationId(MDC.get("correlationId"));
        auditLog.setDetails(limitDetails(details));

        auditLogRepository.save(auditLog);
    }

    /**
     * Получить пользователя из Spring Security Context.
     */
    private Actor resolveActor() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return new Actor(null, "SYSTEM");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return new Actor(
                    jwtUserPrincipal.userId(),
                    jwtUserPrincipal.role()
            );
        }

        if ("anonymousUser".equals(principal)) {
            return new Actor(null, "SYSTEM");
        }

        return new Actor(null, "UNKNOWN");
    }

    private String limitDetails(String details) {
        if (details == null) {
            return null;
        }

        if (details.length() <= MAX_DETAILS_LENGTH) {
            return details;
        }

        return details.substring(0, MAX_DETAILS_LENGTH);
    }

    private record Actor(
            UUID userId,
            String role
    ) {
    }
}
