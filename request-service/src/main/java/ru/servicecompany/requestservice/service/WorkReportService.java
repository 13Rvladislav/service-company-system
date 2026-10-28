
package ru.servicecompany.requestservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.servicecompany.requestservice.dto.request.SaveWorkReportRequest;
import ru.servicecompany.requestservice.dto.response.WorkReportAttachmentResponse;
import ru.servicecompany.requestservice.dto.response.WorkReportResponse;
import ru.servicecompany.requestservice.entity.ServiceRequest;
import ru.servicecompany.requestservice.entity.WorkReport;
import ru.servicecompany.requestservice.entity.WorkReportAttachment;
import ru.servicecompany.requestservice.enums.RequestStatus;
import ru.servicecompany.requestservice.enums.WorkReportStatus;
import ru.servicecompany.requestservice.common.exception.ApiException;
import ru.servicecompany.requestservice.repository.ServiceRequestRepository;
import ru.servicecompany.requestservice.repository.WorkReportAttachmentRepository;
import ru.servicecompany.requestservice.repository.WorkReportRepository;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkReportService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private final WorkReportRepository workReportRepository;
    private final WorkReportAttachmentRepository workReportAttachmentRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    /*
     * Сохранение черновика отчёта мастера.
     */
    @Transactional
    public WorkReportResponse saveDraft(
            UUID userId,
            String role,
            UUID requestId,
            SaveWorkReportRequest input
    ) {
        requireRole(role, "ENGINEER");

        ServiceRequest request = getRequestForUpdate(requestId);
        requireAssignedMaster(request, userId);
        requireReportEditable(request);

        WorkReport report = workReportRepository
                .findByRequestIdForUpdate(requestId)
                .orElseGet(() -> createDraft(request));

        if (report.getStatus() != WorkReportStatus.DRAFT
                && report.getStatus() != WorkReportStatus.REWORK_REQUESTED) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Отправленный отчёт нельзя редактировать"
            );
        }

        report.setDiagnosis(input.diagnosis().trim());
        report.setFailureCause(input.failureCause());
        report.setPerformedWork(input.performedWork().trim());
        report.setWorkResult(input.workResult().trim());
        report.setRecommendations(input.recommendations());
        report.setMasterComment(input.masterComment());

        if (input.completedAt() != null) {
            report.setCompletedAt(input.completedAt());
        }

        report.setStatus(WorkReportStatus.DRAFT);

        return toResponse(workReportRepository.save(report));
    }

    /*
     * Просмотр отчёта.
     */
    @Transactional(readOnly = true)
    public WorkReportResponse getReport(
            UUID userId,
            String role,
            UUID requestId
    ) {
        ServiceRequest request = getRequest(requestId);
        checkCanViewRequest(userId, role, request);

        WorkReport report = workReportRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Отчёт по заявке ещё не создан"
                ));

        checkClientReportVisibility(role, report);

        return toResponse(report);
    }

    /*
     * Отправка отчёта клиенту.
     */
    @Transactional
    public WorkReportResponse submitReport(
            UUID userId,
            String role,
            UUID requestId
    ) {
        requireRole(role, "ENGINEER");

        ServiceRequest request = getRequestForUpdate(requestId);
        requireAssignedMaster(request, userId);
        requireReportEditable(request);

        WorkReport report = workReportRepository
                .findByRequestIdForUpdate(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Сначала создайте отчёт"
                ));

        if (report.getStatus() != WorkReportStatus.DRAFT) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Отчёт уже отправлен или не может быть отправлен"
            );
        }

        if (isBlank(report.getDiagnosis())
                || isBlank(report.getPerformedWork())
                || isBlank(report.getWorkResult())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Заполните диагностику, выполненные работы и результат ремонта"
            );
        }

        if (report.getCompletedAt() == null) {
            report.setCompletedAt(LocalDateTime.now());
        }

        report.setStatus(WorkReportStatus.SUBMITTED);
        report.setSubmittedAt(LocalDateTime.now());

        request.setStatus(RequestStatus.REPORT_SUBMITTED);

        serviceRequestRepository.save(request);
        WorkReport saved = workReportRepository.save(report);

        return toResponse(saved);
    }

    /*
     * Загрузка файла непосредственно в PostgreSQL (BYTEA).
     */
    @Transactional
    public WorkReportAttachmentResponse uploadAttachment(
            UUID userId,
            String role,
            UUID requestId,
            MultipartFile file
    ) {
        requireRole(role, "ENGINEER");

        if (file == null || file.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Файл не выбран или пуст"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Размер файла не должен превышать 10 МБ"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Не удалось определить тип файла"
            );
        }

        contentType = contentType.toLowerCase(Locale.ROOT);

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Разрешены только JPEG, PNG, WEBP и PDF"
            );
        }

        ServiceRequest request = getRequestForUpdate(requestId);
        requireAssignedMaster(request, userId);
        requireReportEditable(request);

        WorkReport report = workReportRepository
                .findByRequestIdForUpdate(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Сначала сохраните черновик отчёта"
                ));

        if (report.getStatus() != WorkReportStatus.DRAFT) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Нельзя добавлять файлы к отправленному отчёту"
            );
        }

        try {
            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null || originalFileName.isBlank()) {
                originalFileName = "attachment";
            } else {
                originalFileName = Paths.get(originalFileName)
                        .getFileName()
                        .toString();
            }

            if (originalFileName.length() > 255) {
                originalFileName = originalFileName.substring(0, 255);
            }

            byte[] fileData = file.getBytes();

            WorkReportAttachment attachment = new WorkReportAttachment();
            attachment.setReportId(report.getId());
            attachment.setOriginalFileName(originalFileName);
            attachment.setContentType(contentType);
            attachment.setSizeBytes(fileData.length);
            attachment.setFileData(fileData);

            WorkReportAttachment saved =
                    workReportAttachmentRepository.save(attachment);

            return new WorkReportAttachmentResponse(
                    saved.getId(),
                    saved.getOriginalFileName(),
                    saved.getContentType(),
                    saved.getSizeBytes(),
                    saved.getUploadedAt()
            );
        } catch (IOException exception) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Не удалось прочитать загруженный файл"
            );
        }
    }

    /*
     * Скачивание файла из PostgreSQL.
     */
    @Transactional(readOnly = true)
    public DownloadedReportFile downloadAttachment(
            UUID userId,
            String role,
            UUID requestId,
            UUID attachmentId
    ) {
        ServiceRequest request = getRequest(requestId);
        checkCanViewRequest(userId, role, request);

        WorkReport report = workReportRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Отчёт не найден"
                ));

        checkClientReportVisibility(role, report);

        WorkReportAttachment attachment =
                workReportAttachmentRepository
                        .findByIdAndReportId(attachmentId, report.getId())
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Вложение не найдено"
                        ));

        return new DownloadedReportFile(
                attachment.getOriginalFileName(),
                attachment.getContentType(),
                attachment.getFileData()
        );
    }

    private WorkReport createDraft(ServiceRequest request) {
        WorkReport report = new WorkReport();

        report.setRequestId(request.getId());
        report.setMasterId(request.getMasterId());
        report.setProblemSnapshot(request.getProblemDescription());
        report.setEquipmentSnapshot("");
        report.setAddressSnapshot("");

        report.setDiagnosis("");
        report.setPerformedWork("");
        report.setWorkResult("");
        report.setStatus(WorkReportStatus.DRAFT);

        return report;
    }

    private ServiceRequest getRequest(UUID requestId) {
        return serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Заявка не найдена"
                ));
    }

    private ServiceRequest getRequestForUpdate(UUID requestId) {
        return serviceRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Заявка не найдена"
                ));
    }

    private void requireAssignedMaster(ServiceRequest request, UUID userId) {
        if (request.getMasterId() == null
                || !request.getMasterId().equals(userId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Заявка не назначена этому мастеру"
            );
        }
    }

    private void requireReportEditable(ServiceRequest request) {
        if (request.getStatus() != RequestStatus.IN_PROGRESS
                && request.getStatus() != RequestStatus.REVISION_REQUIRED) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Отчёт можно редактировать только по заявке в работе или на доработке"
            );
        }
    }

    private void requireRole(String actualRole, String expectedRole) {
        if (!expectedRole.equals(actualRole)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Недостаточно прав для выполнения операции"
            );
        }
    }

    private void checkCanViewRequest(
            UUID userId,
            String role,
            ServiceRequest request
    ) {
        if ("CLIENT".equals(role)) {
            if (!request.getClientId().equals(userId)) {
                throw new ApiException(
                        HttpStatus.FORBIDDEN,
                        "Это заявка другого клиента"
                );
            }
            return;
        }

        if ("ENGINEER".equals(role)) {
            requireAssignedMaster(request, userId);
            return;
        }

        if ("DISPATCHER".equals(role) || "ADMIN".equals(role)) {
            return;
        }

        throw new ApiException(
                HttpStatus.FORBIDDEN,
                "Недостаточно прав для просмотра отчёта"
        );
    }

    private void checkClientReportVisibility(
            String role,
            WorkReport report
    ) {
        if ("CLIENT".equals(role)
                && report.getStatus() != WorkReportStatus.SUBMITTED
                && report.getStatus() != WorkReportStatus.ACCEPTED
                && report.getStatus() != WorkReportStatus.REWORK_REQUESTED) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Отчёт ещё не отправлен клиенту"
            );
        }
    }

    private WorkReportResponse toResponse(WorkReport report) {
        List<WorkReportAttachmentResponse> attachments =
                workReportAttachmentRepository
                        .findAllByReportIdOrderByUploadedAtAsc(report.getId())
                        .stream()
                        .map(attachment -> new WorkReportAttachmentResponse(
                                attachment.getId(),
                                attachment.getOriginalFileName(),
                                attachment.getContentType(),
                                attachment.getSizeBytes(),
                                attachment.getUploadedAt()
                        ))
                        .toList();

        return new WorkReportResponse(
                report.getId(),
                report.getRequestId(),
                report.getMasterId(),
                report.getProblemSnapshot(),
                report.getEquipmentSnapshot(),
                report.getAddressSnapshot(),
                report.getDiagnosis(),
                report.getFailureCause(),
                report.getPerformedWork(),
                report.getWorkResult(),
                report.getRecommendations(),
                report.getMasterComment(),
                report.getCompletedAt(),
                report.getStatus(),
                report.getSubmittedAt(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                attachments
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record DownloadedReportFile(
            String fileName,
            String contentType,
            byte[] content
    ) {}
}
