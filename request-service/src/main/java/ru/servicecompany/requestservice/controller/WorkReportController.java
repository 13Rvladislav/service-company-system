
package ru.servicecompany.requestservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.servicecompany.requestservice.config.JwtUserPrincipal;
import ru.servicecompany.requestservice.dto.request.SaveWorkReportRequest;
import ru.servicecompany.requestservice.dto.response.WorkReportAttachmentResponse;
import ru.servicecompany.requestservice.dto.response.WorkReportResponse;
import ru.servicecompany.requestservice.service.WorkReportService;
import ru.servicecompany.requestservice.service.WorkReportService.DownloadedReportFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests/{requestId}/report")
@RequiredArgsConstructor
public class WorkReportController {

    private final WorkReportService workReportService;

    /*
     * Просмотр отчёта.
     */
    @GetMapping
    public WorkReportResponse getReport(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID requestId
    ) {
        requireAuthenticated(principal);

        return workReportService.getReport(
                principal.userId(),
                principal.role(),
                requestId
        );
    }

    /*
     * Создание или обновление черновика.
     */
    @PutMapping
    public WorkReportResponse saveDraft(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID requestId,
            @Valid @RequestBody SaveWorkReportRequest request
    ) {
        requireAuthenticated(principal);

        return workReportService.saveDraft(
                principal.userId(),
                principal.role(),
                requestId,
                request
        );
    }

    /*
     * Отправка отчёта клиенту.
     */
    @PostMapping("/submit")
    public WorkReportResponse submitReport(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID requestId
    ) {
        requireAuthenticated(principal);

        return workReportService.submitReport(
                principal.userId(),
                principal.role(),
                requestId
        );
    }

    /*
     * Загрузка вложения в PostgreSQL.
     * Имя multipart-поля: file.
     */
    @PostMapping(
            value = "/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public WorkReportAttachmentResponse uploadAttachment(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID requestId,
            @RequestParam("file") MultipartFile file
    ) {
        requireAuthenticated(principal);

        return workReportService.uploadAttachment(
                principal.userId(),
                principal.role(),
                requestId,
                file
        );
    }

    /*
     * Скачивание вложения из PostgreSQL.
     */
    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<ByteArrayResource> downloadAttachment(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID requestId,
            @PathVariable UUID attachmentId
    ) {
        requireAuthenticated(principal);

        DownloadedReportFile file =
                workReportService.downloadAttachment(
                        principal.userId(),
                        principal.role(),
                        requestId,
                        attachmentId
                );

        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(file.contentType());
        } catch (IllegalArgumentException exception) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(mediaType)
                .contentLength(file.content().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .body(new ByteArrayResource(file.content()));
    }

    private void requireAuthenticated(JwtUserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException(
                    "Необходимо авторизоваться"
            );
        }
    }
}
