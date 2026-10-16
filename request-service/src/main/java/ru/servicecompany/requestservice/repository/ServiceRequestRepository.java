
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

    List<ServiceRequest> findAllByClientIdOrderByCreatedAtDesc(UUID clientId);

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
}
