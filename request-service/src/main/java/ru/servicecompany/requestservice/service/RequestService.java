
package ru.servicecompany.requestservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.requestservice.dto.request.CreateRequestRequest;
import ru.servicecompany.requestservice.dto.response.RequestResponse;
import ru.servicecompany.requestservice.entity.ServiceRequest;
import ru.servicecompany.requestservice.enums.RequestStatus;
import ru.servicecompany.requestservice.repository.ServiceRequestRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final ServiceRequestRepository serviceRequestRepository;

    @Transactional
    public RequestResponse createRequest(
            UUID clientId,
            CreateRequestRequest request
    ) {
        ServiceRequest serviceRequest = new ServiceRequest();

        serviceRequest.setClientId(clientId);
        serviceRequest.setAddressId(request.addressId());
        serviceRequest.setEquipmentId(request.equipmentId());
        serviceRequest.setProblemDescription(request.problemDescription());
        serviceRequest.setClientComment(request.clientComment());
        serviceRequest.setPreferredVisitAt(request.preferredVisitAt());

        // Мастер будет назначен отдельным процессом.
        serviceRequest.setMasterId(null);
        serviceRequest.setStatus(RequestStatus.NEW);

        ServiceRequest savedRequest =
                serviceRequestRepository.save(serviceRequest);

        return toResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<RequestResponse> getMyRequests(UUID clientId) {
        return serviceRequestRepository
                .findAllByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RequestResponse toResponse(ServiceRequest request) {
        return new RequestResponse(
                request.getId(),
                request.getClientId(),
                request.getMasterId(),
                request.getAddressId(),
                request.getEquipmentId(),
                request.getProblemDescription(),
                request.getClientComment(),
                request.getStatus(),
                request.getPreferredVisitAt(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
