package ru.servicecompany.requestservice.dto.response;

import ru.servicecompany.requestservice.enums.RequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequestDetailsResponse(
        UUID id,
        UUID addressId,
        String apartment,
        UUID equipmentId,
        UserEquipmentResponse equipment,
        UUID masterId,
        String problemDescription,
        String clientComment,
        RequestStatus status,
        LocalDateTime preferredVisitAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}