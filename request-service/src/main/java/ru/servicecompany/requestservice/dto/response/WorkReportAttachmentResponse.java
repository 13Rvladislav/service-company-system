package ru.servicecompany.requestservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkReportAttachmentResponse(
        UUID id,
        String originalFileName,
        String contentType,
        long sizeBytes,
        LocalDateTime uploadedAt
) {
}