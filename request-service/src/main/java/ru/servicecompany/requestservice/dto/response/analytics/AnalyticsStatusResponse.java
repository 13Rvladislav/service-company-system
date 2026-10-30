package ru.servicecompany.requestservice.dto.response.analytics;

public record AnalyticsStatusResponse(
        String status,
        long count
) {
}