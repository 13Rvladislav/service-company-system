package ru.servicecompany.requestservice.dto.response.analytics;

import java.time.LocalDate;

public record AnalyticsTrendResponse(
        LocalDate date,
        long incoming,
        long completed
) {
}