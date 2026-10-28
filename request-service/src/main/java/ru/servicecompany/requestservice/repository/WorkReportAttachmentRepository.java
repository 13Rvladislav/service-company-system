package ru.servicecompany.requestservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.requestservice.entity.WorkReportAttachment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkReportAttachmentRepository
        extends JpaRepository<WorkReportAttachment, UUID> {

    List<WorkReportAttachment> findAllByReportIdOrderByUploadedAtAsc(
            UUID reportId
    );

    Optional<WorkReportAttachment> findByIdAndReportId(
            UUID id,
            UUID reportId
    );
}