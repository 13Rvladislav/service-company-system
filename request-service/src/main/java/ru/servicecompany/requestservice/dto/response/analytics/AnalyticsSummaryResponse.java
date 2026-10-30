package ru.servicecompany.requestservice.dto.response.analytics;

public record AnalyticsSummaryResponse(
        long total,
        long completed,
        long inProgress,
        long waitingForAssignment,
        long cancelled,
        long reportSubmitted,
        long revisionRequired,
        double completionRate
) {
}