
package ru.servicecompany.requestservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.requestservice.dto.request.CreateRequestRequest;
import ru.servicecompany.requestservice.dto.response.RequestResponse;
import ru.servicecompany.requestservice.entity.ServiceRequest;
import ru.servicecompany.requestservice.enums.RequestStatus;
import ru.servicecompany.requestservice.repository.ServiceRequestRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final ServiceRequestRepository serviceRequestRepository;

    /**
     * Создаёт новую заявку.
     *
     * clientId пока передаётся параметром.
     * Позже будем получать его из авторизованного пользователя.
     */
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

        // Мастер пока не назначен.
        serviceRequest.setMasterId(null);

        // Автоматическое распределение добавим следующим этапом.
        serviceRequest.setStatus(RequestStatus.NEW);

        ServiceRequest savedRequest =
                serviceRequestRepository.save(serviceRequest);

        return toResponse(savedRequest);
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
