
package ru.servicecompany.requestservice.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.common.exception.ApiException;
import ru.servicecompany.requestservice.enums.RequestStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Централизованные правила переходов между статусами заявок.
 */
@Service
public class RequestStatusTransitionService {

    private static final Map<RequestStatus, Set<RequestStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(RequestStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(
                RequestStatus.NEW,
                EnumSet.of(
                        RequestStatus.WAITING_FOR_ASSIGNMENT,
                        RequestStatus.ASSIGNED,
                        RequestStatus.CANCELLED
                )
        );

        ALLOWED_TRANSITIONS.put(
                RequestStatus.WAITING_FOR_ASSIGNMENT,
                EnumSet.of(
                        RequestStatus.ASSIGNED,
                        RequestStatus.CANCELLED
                )
        );

        ALLOWED_TRANSITIONS.put(
                RequestStatus.ASSIGNED,
                EnumSet.of(
                        RequestStatus.WAITING_FOR_ASSIGNMENT,
                        RequestStatus.IN_PROGRESS,
                        RequestStatus.CANCELLED
                )
        );

        ALLOWED_TRANSITIONS.put(
                RequestStatus.IN_PROGRESS,
                EnumSet.of(
                        RequestStatus.REPORT_SUBMITTED
                )
        );

        ALLOWED_TRANSITIONS.put(
                RequestStatus.REPORT_SUBMITTED,
                EnumSet.of(
                        RequestStatus.REVISION_REQUIRED,
                        RequestStatus.COMPLETED
                )
        );

        ALLOWED_TRANSITIONS.put(
                RequestStatus.REVISION_REQUIRED,
                EnumSet.of(
                        RequestStatus.REPORT_SUBMITTED
                )
        );

        ALLOWED_TRANSITIONS.put(
                RequestStatus.COMPLETED,
                EnumSet.noneOf(RequestStatus.class)
        );

        ALLOWED_TRANSITIONS.put(
                RequestStatus.CANCELLED,
                EnumSet.noneOf(RequestStatus.class)
        );
    }

    /**
     * Проверяет, разрешён ли переход.
     */
    public void validateTransition(
            RequestStatus currentStatus,
            RequestStatus newStatus
    ) {
        if (currentStatus == null || newStatus == null) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Статус заявки не может быть пустым"
            );
        }

        Set<RequestStatus> allowed =
                ALLOWED_TRANSITIONS.getOrDefault(
                        currentStatus,
                        EnumSet.noneOf(RequestStatus.class)
                );

        if (!allowed.contains(newStatus)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Недопустимый переход статуса заявки: "
                            + currentStatus + " -> " + newStatus
            );
        }
    }

    /**
     * Проверяет переход и возвращает новый статус.
     */
    public RequestStatus transition(
            RequestStatus currentStatus,
            RequestStatus newStatus
    ) {
        validateTransition(currentStatus, newStatus);
        return newStatus;
    }

    /**
     * Проверяет, разрешён ли переход без выбрасывания исключения.
     */
    public boolean canTransition(
            RequestStatus currentStatus,
            RequestStatus newStatus
    ) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        return ALLOWED_TRANSITIONS
                .getOrDefault(
                        currentStatus,
                        EnumSet.noneOf(RequestStatus.class)
                )
                .contains(newStatus);
    }
}
