package ru.servicecompany.requestservice.dto.response.analytics;

import java.util.UUID;

public record AnalyticsZoneResponse(
        UUID zoneId,
        long total,
        long completed,
        long inProgress,
        long waitingForAssignment,
        double completionRate
) {
}