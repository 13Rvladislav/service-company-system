package ru.servicecompany.requestservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.requestservice.common.exception.ApiException;
import ru.servicecompany.requestservice.dto.request.CreateRequestRequest;
import ru.servicecompany.requestservice.dto.request.UpdateRequestRequest;
import ru.servicecompany.requestservice.dto.response.AddressResponse;
import ru.servicecompany.requestservice.dto.response.MyRequestResponse;
import ru.servicecompany.requestservice.dto.response.RequestDetailsResponse;
import ru.servicecompany.requestservice.dto.response.RequestResponse;
import ru.servicecompany.requestservice.dto.response.UserEquipmentResponse;
import ru.servicecompany.requestservice.entity.ServiceRequest;
import ru.servicecompany.requestservice.enums.RequestStatus;
import ru.servicecompany.requestservice.kafka.AddressByIdRequestService;
import ru.servicecompany.requestservice.kafka.EquipmentByIdRequestService;
import ru.servicecompany.requestservice.repository.ServiceRequestRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final ServiceRequestRepository serviceRequestRepository;

    private final EquipmentByIdRequestService equipmentByIdRequestService;

    private final AddressByIdRequestService addressByIdRequestService;

    /**
     * Создание заявки клиентом.
     */
    @Transactional
    public RequestResponse createRequest(
            UUID clientId,
            CreateRequestRequest request
    ) {
        if (request.equipmentId() == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Необходимо выбрать оборудование"
            );
        }

        if (request.addressId() == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Необходимо выбрать адрес"
            );
        }

        ServiceRequest serviceRequest = new ServiceRequest();

        serviceRequest.setClientId(clientId);
        serviceRequest.setAddressId(request.addressId());
        serviceRequest.setEquipmentId(request.equipmentId());
        serviceRequest.setProblemDescription(request.problemDescription());
        serviceRequest.setClientComment(request.clientComment());
        serviceRequest.setPreferredVisitAt(request.preferredVisitAt());
        serviceRequest.setMasterId(null);
        serviceRequest.setStatus(RequestStatus.NEW);

        return toResponse(
                serviceRequestRepository.save(serviceRequest)
        );
    }

    /**
     * Получение подробной карточки заявки клиентом.
     *
     * Адрес и оборудование запрашиваются отдельно
     * через Kafka по ID, сохранённым в заявке.
     */
    @Transactional(readOnly = true)
    public RequestDetailsResponse getRequestDetails(
            UUID clientId,
            UUID requestId
    ) {
        ServiceRequest request = serviceRequestRepository
                .findByIdAndClientId(requestId, clientId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Заявка не найдена"
                ));

        // Получаем адрес заявки.
        AddressResponse address = getAddress(
                request.getAddressId()
        );

        // Получаем оборудование заявки.
        UserEquipmentResponse equipment = getEquipment(
                clientId,
                request.getEquipmentId()
        );

        return new RequestDetailsResponse(
                request.getId(),

                request.getAddressId(),
                address,

                request.getEquipmentId(),
                equipment,

                request.getMasterId(),

                request.getProblemDescription(),
                request.getClientComment(),

                request.getStatus(),

                request.getPreferredVisitAt(),

                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }

    /**
     * Получение адреса по ID дома.
     */
    private AddressResponse getAddress(UUID addressId) {

        if (addressId == null) {
            return null;
        }

        return addressByIdRequestService
                .getAddressById(addressId);
    }

    /**
     * Получение оборудования по ID.
     */
    private UserEquipmentResponse getEquipment(
            UUID clientId,
            UUID equipmentId
    ) {
        if (equipmentId == null) {
            return null;
        }

        return equipmentByIdRequestService
                .getEquipmentById(clientId, equipmentId)
                .getEquipment();
    }

    /**
     * Получение списка заявок клиента.
     */
    @Transactional(readOnly = true)
    public List<MyRequestResponse> getMyRequests(UUID clientId) {

        return serviceRequestRepository
                .findAllByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(request -> new MyRequestResponse(
                        request.getId(),
                        request.getCreatedAt(),
                        request.getStatus()
                ))
                .toList();
    }

    /**
     * Редактирование заявки клиентом.
     *
     * Разрешено до принятия заявки мастером.
     */
    @Transactional
    public RequestResponse updateRequest(
            UUID clientId,
            UUID requestId,
            UpdateRequestRequest update
    ) {
        ServiceRequest request = getRequestForUpdate(requestId);

        checkOwner(request, clientId);
        checkCanBeChanged(request);

        if (update.problemDescription() == null
                || update.problemDescription().isBlank()) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Описание проблемы обязательно"
            );
        }

        request.setProblemDescription(
                update.problemDescription().trim()
        );

        // Необязательный комментарий.
        request.setClientComment(
                normalizeComment(update.clientComment())
        );

        // Предпочтительное время визита.
        request.setPreferredVisitAt(
                update.preferredVisitAt()
        );

        return toResponse(
                serviceRequestRepository.save(request)
        );
    }

    /**
     * Отмена заявки клиентом.
     *
     * Разрешено до принятия заявки мастером.
     */
    @Transactional
    public RequestResponse cancelRequest(
            UUID clientId,
            UUID requestId
    ) {
        ServiceRequest request = getRequestForUpdate(requestId);

        checkOwner(request, clientId);
        checkCanBeChanged(request);

        request.setStatus(RequestStatus.CANCELLED);

        return toResponse(
                serviceRequestRepository.save(request)
        );
    }

    /**
     * Получение заявки с блокировкой для изменения.
     */
    private ServiceRequest getRequestForUpdate(UUID requestId) {

        return serviceRequestRepository
                .findByIdForUpdate(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Заявка не найдена"
                ));
    }

    /**
     * Проверка владельца заявки.
     */
    private void checkOwner(
            ServiceRequest request,
            UUID clientId
    ) {
        if (!request.getClientId().equals(clientId)) {

            // Не раскрываем существование чужой заявки.
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Заявка не найдена"
            );
        }
    }

    /**
     * Проверка возможности редактирования или отмены.
     */
    private void checkCanBeChanged(ServiceRequest request) {

        boolean editable = switch (request.getStatus()) {
            case NEW, WAITING_FOR_ASSIGNMENT, ASSIGNED -> true;
            default -> false;
        };

        if (!editable) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Редактировать или отменять можно только заявку, "
                            + "которую мастер ещё не принял"
            );
        }
    }

    /**
     * Нормализация комментария.
     */
    private String normalizeComment(String comment) {

        if (comment == null || comment.isBlank()) {
            return null;
        }

        return comment.trim();
    }

    /**
     * Преобразование заявки в обычный ответ.
     */
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