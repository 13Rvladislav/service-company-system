
package ru.servicecompany.auth.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.auth.entity.AuditLog;
import ru.servicecompany.auth.enums.AuditResult;
import ru.servicecompany.auth.repository.AuditLogRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final int MAX_DETAILS_LENGTH = 4000;

    private final AuditLogRepository auditLogRepository;

    /**
     * Запись события авторизации.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuthEvent(
            String action,
            UUID actorId,
            String actorRole,
            AuditResult result,
            String details
    ) {

        AuditLog auditLog = new AuditLog();

        auditLog.setActorId(actorId);
        auditLog.setActorRole(actorRole);
        auditLog.setAction(action);
        auditLog.setEntityType("AUTHENTICATION");
        auditLog.setEntityId(actorId);
        auditLog.setResult(result);
        auditLog.setCorrelationId(MDC.get("correlationId"));
        auditLog.setDetails(limitDetails(details));

        auditLogRepository.save(auditLog);
    }

    /**
     * Запись события управления учетной записью пользователя.
     *
     * actorId — пользователь, выполнивший действие.
     * entityId — пользователь, над которым выполнено действие.
     *
     * REQUIRES_NEW позволяет сохранить аудит независимо
     * от основной транзакции бизнес-операции.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUserManagementEvent(
            String action,
            UUID actorId,
            String actorRole,
            UUID entityId,
            AuditResult result,
            String details
    ) {

        AuditLog auditLog = new AuditLog();

        auditLog.setActorId(actorId);
        auditLog.setActorRole(actorRole);
        auditLog.setAction(action);
        auditLog.setEntityType("USER_ACCOUNT");
        auditLog.setEntityId(entityId);
        auditLog.setResult(result);
        auditLog.setCorrelationId(MDC.get("correlationId"));
        auditLog.setDetails(limitDetails(details));

        auditLogRepository.save(auditLog);
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
}
