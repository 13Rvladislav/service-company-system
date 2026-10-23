package ru.servicecompany.requestservice.dto.response;

import ru.servicecompany.requestservice.enums.RequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ManagementRequestResponse(
        UUID id,
        UUID clientId,
        UUID masterId,
        UUID addressId,
        UUID equipmentId,
        String apartment,
        String problemDescription,
        RequestStatus status,
        LocalDateTime preferredVisitAt,
        LocalDateTime createdAt
) {
}