package ru.servicecompany.requestservice.dto.response;

public record WorkReportLookupResponse(
        boolean exists,
        WorkReportResponse report
) {
    public static WorkReportLookupResponse notExists() {
        return new WorkReportLookupResponse(false, null);
    }

    public static WorkReportLookupResponse exists(
            WorkReportResponse report
    ) {
        return new WorkReportLookupResponse(true, report);
    }
}