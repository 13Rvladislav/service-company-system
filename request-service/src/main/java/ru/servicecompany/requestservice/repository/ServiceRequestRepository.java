
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

    List<ServiceRequest> findAllByMasterId(UUID masterId);

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
    /**
     * Блокирует заявку на время транзакции.
     * Это предотвращает одновременное изменение её статуса
     * другим запросом, пока выполняется редактирование или отмена.
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
}
