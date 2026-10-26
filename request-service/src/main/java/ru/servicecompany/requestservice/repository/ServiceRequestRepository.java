
package ru.servicecompany.requestservice.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.servicecompany.requestservice.entity.ServiceRequest;
import ru.servicecompany.requestservice.enums.RequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, UUID> {

    List<ServiceRequest> findAllByClientIdOrderByCreatedAtDesc(
            UUID clientId
    );

    List<ServiceRequest> findAllByOrderByCreatedAtDesc();

    List<ServiceRequest> findAllByMasterId(UUID masterId);

    List<ServiceRequest> findAllByMasterIdOrderByCreatedAtDesc(
            UUID masterId
    );

    List<ServiceRequest> findAllByStatus(RequestStatus status);

    List<ServiceRequest> findAllByClientIdAndStatus(
            UUID clientId,
            RequestStatus status
    );

    List<ServiceRequest> findAllByMasterIdAndStatus(
            UUID masterId,
            RequestStatus status
    );

    Optional<ServiceRequest> findByIdAndClientId(
            UUID id,
            UUID clientId
    );

    Optional<ServiceRequest> findByIdAndMasterId(
            UUID id,
            UUID masterId
    );

    /**
     * Блокирует заявку на время транзакции.
     * Предотвращает параллельное изменение заявки.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select request
            from ServiceRequest request
            where request.id = :requestId
            """)
    Optional<ServiceRequest> findByIdForUpdate(
            @Param("requestId") UUID requestId
    );

    /**
     * Подсчёт активных заявок мастера.
     */
    @Query("""
            select count(request)
            from ServiceRequest request
            where request.masterId = :masterId
              and request.status not in (
                  ru.servicecompany.requestservice.enums.RequestStatus.COMPLETED,
                  ru.servicecompany.requestservice.enums.RequestStatus.CANCELLED
              )
            """)
    long countActiveRequestsByMasterId(
            @Param("masterId") UUID masterId
    );
}
