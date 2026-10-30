
package ru.servicecompany.requestservice.dto.response;

import ru.servicecompany.requestservice.enums.WorkReportStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WorkReportResponse(
        UUID id,
        UUID requestId,
        UUID masterId,

        String problemSnapshot,
        String equipmentSnapshot,
        String addressSnapshot,

        String diagnosis,
        String failureCause,
        String performedWork,
        String workResult,
        String recommendations,
        String masterComment,
        String clientFeedback,

        LocalDateTime completedAt,
        WorkReportStatus status,
        LocalDateTime submittedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        List<WorkReportAttachmentResponse> attachments
) {
}
