
package ru.servicecompany.requestservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.servicecompany.requestservice.entity.ServiceRequest;
import ru.servicecompany.requestservice.enums.RequestStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, UUID> {

    /**
     * Все заявки конкретного клиента.
     */
    List<ServiceRequest> findAllByClientId(UUID clientId);

    /**
     * Все заявки, назначенные конкретному мастеру.
     */
    List<ServiceRequest> findAllByMasterId(UUID masterId);

    /**
     * Все заявки с определённым статусом.
     */
    List<ServiceRequest> findAllByStatus(RequestStatus status);

    /**
     * Заявки клиента с определённым статусом.
     */
    List<ServiceRequest> findAllByClientIdAndStatus(
            UUID clientId,
            RequestStatus status
    );

    /**
     * Заявки мастера с определённым статусом.
     */
    List<ServiceRequest> findAllByMasterIdAndStatus(
            UUID masterId,
            RequestStatus status
    );
}
