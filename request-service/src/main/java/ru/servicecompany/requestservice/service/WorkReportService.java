
package ru.servicecompany.requestservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.servicecompany.requestservice.common.exception.ApiException;
import ru.servicecompany.requestservice.dto.request.SaveWorkReportRequest;
import ru.servicecompany.requestservice.dto.response.*;
import ru.servicecompany.requestservice.entity.ServiceRequest;
import ru.servicecompany.requestservice.entity.WorkReport;
import ru.servicecompany.requestservice.entity.WorkReportAttachment;
import ru.servicecompany.requestservice.enums.RequestStatus;
import ru.servicecompany.requestservice.enums.WorkReportStatus;
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

@Slf4j
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

    private final RequestStatusTransitionService requestStatusTransitionService;
    private final WorkReportRepository workReportRepository;
    private final WorkReportAttachmentRepository workReportAttachmentRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final RequestService requestService;
    private final WorkReportPdfService workReportPdfService;

    /**
     * Создание или сохранение черновика отчёта мастера.
     * Разрешено сохранять отчёт в статусах IN_PROGRESS
     * и REVISION_REQUIRED.
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
                .orElseGet(() -> createDraft(request, userId, role));

        WorkReportStatus previousReportStatus = report.getStatus();

        if (report.getStatus() != WorkReportStatus.DRAFT
                && report.getStatus() != WorkReportStatus.REWORK_REQUESTED) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Редактировать можно только черновик или отчёт, возвращённый на доработку"
            );
        }

        report.setDiagnosis(input.diagnosis().trim());
        report.setFailureCause(normalize(input.failureCause()));
        report.setPerformedWork(input.performedWork().trim());
        report.setWorkResult(input.workResult().trim());
        report.setRecommendations(normalize(input.recommendations()));
        report.setMasterComment(normalize(input.masterComment()));

        // После начала редактирования возвращённый отчёт становится черновиком.
        report.setStatus(WorkReportStatus.DRAFT);

        if (input.completedAt() != null) {
            report.setCompletedAt(input.completedAt());
        }

        WorkReport savedReport = workReportRepository.save(report);

        log.info(
                "BUSINESS_EVENT event=WORK_REPORT_DRAFT_SAVED requestId={} reportId={} masterId={} previousReportStatus={} newReportStatus={} requestStatus={}",
                requestId,
                savedReport.getId(),
                userId,
                previousReportStatus,
                savedReport.getStatus(),
                request.getStatus()
        );

        return toResponse(savedReport);
    }

    /**
     * Получение отчёта без 404, если отчёт ещё не создан.
     * GET не создаёт запись в базе данных.
     */
    @Transactional(readOnly = true)
    public WorkReportLookupResponse getReportIfExists(
            UUID userId,
            String role,
            UUID requestId
    ) {
        ServiceRequest request = getRequest(requestId);
        checkCanViewRequest(userId, role, request);

        return workReportRepository.findByRequestId(requestId)
                .map(report -> {
                    checkClientReportVisibility(role, report);
                    return WorkReportLookupResponse.exists(toResponse(report));
                })
                .orElseGet(WorkReportLookupResponse::notExists);
    }

    /**
     * Удаление вложения из черновика отчёта.
     */
    @Transactional
    public void deleteAttachment(
            UUID userId,
            String role,
            UUID requestId,
            UUID attachmentId
    ) {
        requireRole(role, "ENGINEER");

        ServiceRequest request = getRequestForUpdate(requestId);
        requireAssignedMaster(request, userId);
        requireReportEditable(request);

        WorkReport report = workReportRepository
                .findByRequestIdForUpdate(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Отчёт по заявке не найден"
                ));

        if (report.getStatus() != WorkReportStatus.DRAFT) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Нельзя удалять вложения из отправленного отчёта"
            );
        }

        WorkReportAttachment attachment =
                workReportAttachmentRepository
                        .findByIdAndReportId(attachmentId, report.getId())
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Вложение не найдено в этом отчёте"
                        ));

        workReportAttachmentRepository.delete(attachment);

        log.info(
                "BUSINESS_EVENT event=WORK_REPORT_ATTACHMENT_DELETED requestId={} reportId={} attachmentId={} masterId={}",
                requestId,
                report.getId(),
                attachmentId,
                userId
        );
    }

    /**
     * Отправка отчёта мастером.
     * Поддерживает первоначальную отправку и повторную отправку
     * после исправления замечаний клиента.
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
                        "Сначала сохраните черновик отчёта"
                ));

        if (report.getStatus() != WorkReportStatus.DRAFT) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
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

        RequestStatus previousRequestStatus = request.getStatus();
        WorkReportStatus previousReportStatus = report.getStatus();

        if (report.getCompletedAt() == null) {
            report.setCompletedAt(LocalDateTime.now());
        }

        report.setStatus(WorkReportStatus.SUBMITTED);
        report.setSubmittedAt(LocalDateTime.now());

        request.setStatus(
                requestStatusTransitionService.transition(
                        previousRequestStatus,
                        RequestStatus.REPORT_SUBMITTED
                )
        );

        serviceRequestRepository.save(request);

        WorkReport savedReport = workReportRepository.save(report);

        log.info(
                "BUSINESS_EVENT event=WORK_REPORT_SUBMITTED requestId={} reportId={} masterId={} previousRequestStatus={} newRequestStatus={} previousReportStatus={} newReportStatus={}",
                requestId,
                savedReport.getId(),
                userId,
                previousRequestStatus,
                request.getStatus(),
                previousReportStatus,
                savedReport.getStatus()
        );

        return toResponse(savedReport);
    }

    /**
     * Клиент принимает выполненные работы.
     * Отчёт получает статус ACCEPTED, заявка — COMPLETED.
     */
    @Transactional
    public WorkReportResponse acceptWork(
            UUID userId,
            String role,
            UUID requestId
    ) {
        requireRole(role, "CLIENT");

        ServiceRequest request = getRequestForUpdate(requestId);
        requireClientOwnsRequest(request, userId);
        requireSubmittedReportStatus(request);

        WorkReport report = workReportRepository
                .findByRequestIdForUpdate(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Отчёт по заявке не найден"
                ));

        if (report.getStatus() != WorkReportStatus.SUBMITTED) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Отчёт не ожидает подтверждения клиента"
            );
        }

        RequestStatus previousRequestStatus = request.getStatus();
        WorkReportStatus previousReportStatus = report.getStatus();

        report.setStatus(WorkReportStatus.ACCEPTED);

        request.setStatus(
                requestStatusTransitionService.transition(
                        previousRequestStatus,
                        RequestStatus.COMPLETED
                )
        );

        serviceRequestRepository.save(request);

        WorkReport savedReport = workReportRepository.save(report);

        log.info(
                "BUSINESS_EVENT event=WORK_ACCEPTED_BY_CLIENT requestId={} reportId={} clientId={} masterId={} previousRequestStatus={} newRequestStatus={} previousReportStatus={} newReportStatus={}",
                requestId,
                savedReport.getId(),
                userId,
                savedReport.getMasterId(),
                previousRequestStatus,
                request.getStatus(),
                previousReportStatus,
                savedReport.getStatus()
        );

        return toResponse(savedReport);
    }

    /**
     * Клиент возвращает отчёт на доработку.
     * Причина сохраняется в clientFeedback.
     */
    @Transactional
    public WorkReportResponse requestRework(
            UUID userId,
            String role,
            UUID requestId,
            String reason
    ) {
        requireRole(role, "CLIENT");

        if (reason == null || reason.isBlank()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Укажите причину возврата отчёта на доработку"
            );
        }

        String normalizedReason = reason.trim();

        if (normalizedReason.length() > 5000) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Причина доработки не должна превышать 5000 символов"
            );
        }

        ServiceRequest request = getRequestForUpdate(requestId);
        requireClientOwnsRequest(request, userId);
        requireSubmittedReportStatus(request);

        WorkReport report = workReportRepository
                .findByRequestIdForUpdate(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Отчёт по заявке не найден"
                ));

        if (report.getStatus() != WorkReportStatus.SUBMITTED) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Отчёт не ожидает проверки клиента"
            );
        }

        RequestStatus previousRequestStatus = request.getStatus();
        WorkReportStatus previousReportStatus = report.getStatus();

        report.setClientFeedback(normalizedReason);
        report.setStatus(WorkReportStatus.REWORK_REQUESTED);

        request.setStatus(
                requestStatusTransitionService.transition(
                        previousRequestStatus,
                        RequestStatus.REVISION_REQUIRED
                )
        );

        serviceRequestRepository.save(request);

        WorkReport savedReport = workReportRepository.save(report);

        log.warn(
                "BUSINESS_EVENT event=WORK_RETURNED_FOR_REWORK requestId={} reportId={} clientId={} masterId={} previousRequestStatus={} newRequestStatus={} previousReportStatus={} newReportStatus={}",
                requestId,
                savedReport.getId(),
                userId,
                savedReport.getMasterId(),
                previousRequestStatus,
                request.getStatus(),
                previousReportStatus,
                savedReport.getStatus()
        );

        return toResponse(savedReport);
    }

    /**
     * Загрузка вложения в PostgreSQL.
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
                    HttpStatus.CONFLICT,
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

            log.info(
                    "BUSINESS_EVENT event=WORK_REPORT_ATTACHMENT_UPLOADED requestId={} reportId={} attachmentId={} masterId={} contentType={} sizeBytes={}",
                    requestId,
                    report.getId(),
                    saved.getId(),
                    userId,
                    saved.getContentType(),
                    saved.getSizeBytes()
            );

            return new WorkReportAttachmentResponse(
                    saved.getId(),
                    saved.getOriginalFileName(),
                    saved.getContentType(),
                    saved.getSizeBytes(),
                    saved.getUploadedAt()
            );
        } catch (IOException exception) {
            log.error(
                    "Failed to read work report attachment: requestId={}, masterId={}",
                    requestId,
                    userId,
                    exception
            );

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Не удалось прочитать загруженный файл"
            );
        }
    }

    /**
     * Скачивание вложения из PostgreSQL.
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

    /**
     * Создание черновика с сохранением снимков исходных данных заявки.
     */
    private WorkReport createDraft(
            ServiceRequest request,
            UUID userId,
            String role
    ) {
        RequestDetailsResponse details = requestService.getRequestDetails(
                userId,
                role,
                request.getId()
        );

        WorkReport report = new WorkReport();

        report.setRequestId(request.getId());
        report.setMasterId(request.getMasterId());

        report.setProblemSnapshot(firstNonBlank(
                details.problemDescription(),
                request.getProblemDescription(),
                "Описание проблемы отсутствует"
        ));

        report.setEquipmentSnapshot(
                buildEquipmentSnapshot(details.equipment())
        );

        report.setAddressSnapshot(
                buildAddressSnapshot(details)
        );

        report.setDiagnosis("");
        report.setPerformedWork("");
        report.setWorkResult("");
        report.setStatus(WorkReportStatus.DRAFT);

        return report;
    }

    private String buildEquipmentSnapshot(UserEquipmentResponse equipment) {
        if (equipment == null) {
            return "Информация об оборудовании отсутствует";
        }

        String result = String.join(
                " ",
                nonBlank(equipment.getEquipmentTypeName()),
                nonBlank(equipment.getManufacturer()),
                nonBlank(equipment.getModel())
        ).trim();

        if (result.isBlank()) {
            result = nonBlank(equipment.getDescription());
        }

        return result.isBlank()
                ? "Информация об оборудовании отсутствует"
                : result;
    }

    private String buildAddressSnapshot(RequestDetailsResponse details) {
        AddressResponse address = details.address();

        if (address == null) {
            return "Адрес заявки не указан";
        }

        StringBuilder result = new StringBuilder();

        appendPart(result, address.cityName());
        appendPart(result, address.streetName());

        if (address.houseNumber() != null
                && !address.houseNumber().isBlank()) {
            appendPart(result, "д. " + address.houseNumber().trim());
        }

        String apartment = firstNonBlank(
                details.apartment(),
                address.apartment()
        );

        if (apartment != null) {
            appendPart(result, "кв. " + apartment);
        }

        return result.isEmpty()
                ? "Адрес заявки не указан"
                : result.toString();
    }

    private void appendPart(StringBuilder result, String part) {
        if (part == null || part.isBlank()) {
            return;
        }

        if (!result.isEmpty()) {
            result.append(", ");
        }

        result.append(part.trim());
    }

    private String nonBlank(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
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

    private void requireAssignedMaster(
            ServiceRequest request,
            UUID userId
    ) {
        if (request.getMasterId() == null
                || !request.getMasterId().equals(userId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Заявка не назначена этому мастеру"
            );
        }
    }

    private void requireClientOwnsRequest(
            ServiceRequest request,
            UUID userId
    ) {
        if (request.getClientId() == null
                || !request.getClientId().equals(userId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Это заявка другого клиента"
            );
        }
    }

    private void requireSubmittedReportStatus(ServiceRequest request) {
        if (request.getStatus() != RequestStatus.REPORT_SUBMITTED) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Операция доступна только после отправки отчёта мастером"
            );
        }
    }

    private void requireReportEditable(ServiceRequest request) {
        if (request.getStatus() != RequestStatus.IN_PROGRESS
                && request.getStatus() != RequestStatus.REVISION_REQUIRED) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Редактировать отчёт можно только во время выполнения работ или после возврата на доработку"
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
            requireClientOwnsRequest(request, userId);
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
                report.getClientFeedback(),
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
    ) {
    }

    /**
     * Формирование PDF-отчёта по конкретной заявке.
     */
    @Transactional(readOnly = true)
    public PdfReportFile downloadPdf(
            UUID userId,
            String role,
            UUID requestId
    ) {
        ServiceRequest request = getRequest(requestId);

        checkCanViewRequest(userId, role, request);

        WorkReport report = workReportRepository
                .findByRequestId(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Отчёт по заявке ещё не создан"
                ));

        checkClientReportVisibility(role, report);

        if (report.getStatus() == WorkReportStatus.DRAFT) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Нельзя скачать PDF: отчёт ещё не отправлен"
            );
        }

        RequestDetailsResponse details =
                requestService.getRequestDetails(
                        userId,
                        role,
                        requestId
                );

        byte[] pdf = workReportPdfService.generate(details, report);

        return new PdfReportFile(
                "request-report-" + requestId + ".pdf",
                pdf
        );
    }

    /**
     * Готовый PDF-документ.
     */
    public record PdfReportFile(
            String fileName,
            byte[] content
    ) {
    }
}
