package ru.servicecompany.requestservice.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.servicecompany.requestservice.entity.WorkReport;

import java.util.Optional;
import java.util.UUID;

public interface WorkReportRepository
        extends JpaRepository<WorkReport, UUID> {

    Optional<WorkReport> findByRequestId(UUID requestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select report
            from WorkReport report
            where report.requestId = :requestId
            """)
    Optional<WorkReport> findByRequestIdForUpdate(
            @Param("requestId") UUID requestId
    );
}