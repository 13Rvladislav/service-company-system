package ru.servicecompany.requestservice.dto.response.analytics;

import java.time.LocalDate;
import java.util.List;

public record AnalyticsResponse(
        LocalDate from,
        LocalDate to,
        AnalyticsSummaryResponse summary,
        List<AnalyticsTrendResponse> trend,
        List<AnalyticsStatusResponse> statuses,
        List<AnalyticsMasterResponse> masters,
        List<AnalyticsZoneResponse> zones,
        List<AnalyticsRecentRequestResponse> recentRequests
) {
}