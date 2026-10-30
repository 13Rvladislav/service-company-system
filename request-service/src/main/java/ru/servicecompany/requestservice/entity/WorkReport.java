
package ru.servicecompany.requestservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.servicecompany.requestservice.enums.WorkReportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "work_reports",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_work_report_request",
                columnNames = "request_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class WorkReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "request_id", nullable = false, updatable = false)
    private UUID requestId;

    @Column(name = "master_id", nullable = false, updatable = false)
    private UUID masterId;

    @Column(name = "problem_snapshot", nullable = false, columnDefinition = "TEXT")
    private String problemSnapshot;

    @Column(name = "equipment_snapshot", columnDefinition = "TEXT")
    private String equipmentSnapshot;

    @Column(name = "address_snapshot", columnDefinition = "TEXT")
    private String addressSnapshot;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "failure_cause", columnDefinition = "TEXT")
    private String failureCause;

    @Column(name = "performed_work", nullable = false, columnDefinition = "TEXT")
    private String performedWork;

    @Column(name = "work_result", nullable = false, columnDefinition = "TEXT")
    private String workResult;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "master_comment", columnDefinition = "TEXT")
    private String masterComment;

    @Column(name = "client_feedback", columnDefinition = "TEXT")
    private String clientFeedback;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkReportStatus status = WorkReportStatus.DRAFT;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = WorkReportStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
