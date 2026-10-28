package ru.servicecompany.requestservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record SaveWorkReportRequest(

        @NotBlank(message = "Укажи результаты диагностики")
        @Size(max = 10000)
        String diagnosis,

        @Size(max = 10000)
        String failureCause,

        @NotBlank(message = "Укажи выполненные работы")
        @Size(max = 10000)
        String performedWork,

        @NotBlank(message = "Укажи результат ремонта")
        @Size(max = 10000)
        String workResult,

        @Size(max = 10000)
        String recommendations,

        @Size(max = 10000)
        String masterComment,

        LocalDateTime completedAt
) {
}