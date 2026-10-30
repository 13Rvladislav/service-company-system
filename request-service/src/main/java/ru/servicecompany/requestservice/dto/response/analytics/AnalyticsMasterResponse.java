package ru.servicecompany.requestservice.dto.response.analytics;

import java.util.UUID;

public record AnalyticsMasterResponse(
        UUID masterId,
        long total,
        long completed,
        long inProgress,
        long cancelled,
        double completionRate
) {
}