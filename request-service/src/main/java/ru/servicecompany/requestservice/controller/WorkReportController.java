
package ru.servicecompany.requestservice.controller;

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
import ru.servicecompany.requestservice.dto.response.WorkReportLookupResponse;
import ru.servicecompany.requestservice.dto.response.WorkReportResponse;
import ru.servicecompany.requestservice.service.WorkReportService;
import ru.servicecompany.requestservice.service.WorkReportService.DownloadedReportFile;

import jakarta.validation.Valid;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests/{requestId}/report")
@RequiredArgsConstructor
public class WorkReportController {

    private final WorkReportService workReportService;

    @GetMapping
    public WorkReportLookupResponse getReport(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID requestId
    ) {
        requireAuthenticated(principal);

        return workReportService.getReportIfExists(
                principal.userId(),
                principal.role(),
                requestId
        );
    }

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

    @DeleteMapping("/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttachment(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID requestId,
            @PathVariable UUID attachmentId
    ) {
        requireAuthenticated(principal);

        workReportService.deleteAttachment(
                principal.userId(),
                principal.role(),
                requestId,
                attachmentId
        );
    }

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

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(file.content().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .body(new ByteArrayResource(file.content()));
    }

    /**
     * Скачать PDF-отчёт по конкретной заявке.
     */
    @GetMapping("/pdf")
    public ResponseEntity<ByteArrayResource> downloadPdf(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID requestId
    ) {
        requireAuthenticated(principal);

        WorkReportService.PdfReportFile file = workReportService.downloadPdf(
                principal.userId(),
                principal.role(),
                requestId
        );

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
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
