package ru.servicecompany.requestservice.dto.response.analytics;

import ru.servicecompany.requestservice.enums.RequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnalyticsRecentRequestResponse(
        UUID id,
        UUID masterId,
        UUID zoneId,
        RequestStatus status,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
}