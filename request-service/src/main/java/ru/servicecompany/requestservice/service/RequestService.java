
package ru.servicecompany.requestservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.requestservice.common.exception.ApiException;
import ru.servicecompany.requestservice.dto.request.CreateRequestRequest;
import ru.servicecompany.requestservice.dto.request.UpdateRequestRequest;
import ru.servicecompany.requestservice.dto.response.*;
import ru.servicecompany.requestservice.entity.ServiceRequest;
import ru.servicecompany.requestservice.enums.RequestStatus;
import ru.servicecompany.requestservice.kafka.*;
import ru.servicecompany.requestservice.kafka.event.ProfileResponseEvent;
import ru.servicecompany.requestservice.repository.ServiceRequestRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final EquipmentByIdRequestService equipmentByIdRequestService;
    private final AddressByIdRequestService addressByIdRequestService;
    private final MasterByIdRequestService masterByIdRequestService;
    private final MasterAssignmentCandidatesRequestService
            masterAssignmentCandidatesRequestService;
    private final ProfileRequestService profileRequestService;

    /**
     * Создание заявки клиентом с автоматическим назначением мастера.
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

        AddressResponse address = getAddress(request.addressId());

        UserEquipmentResponse equipment = getEquipment(
                clientId,
                request.equipmentId()
        );

        if (address == null) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Адрес не найден"
            );
        }

        if (equipment == null) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Оборудование не найдено"
            );
        }

        ServiceRequest serviceRequest = new ServiceRequest();

        serviceRequest.setClientId(clientId);
        serviceRequest.setAddressId(request.addressId());
        serviceRequest.setApartment(
                normalizeApartment(request.apartment())
        );
        serviceRequest.setEquipmentId(request.equipmentId());
        serviceRequest.setProblemDescription(
                request.problemDescription().trim()
        );
        serviceRequest.setClientComment(
                normalizeComment(request.clientComment())
        );
        serviceRequest.setPreferredVisitAt(
                request.preferredVisitAt()
        );

        List<UUID> masterIds =
                masterAssignmentCandidatesRequestService.getCandidates(
                        address.zoneId(),
                        equipment.getEquipmentTypeId()
                );

        UUID masterId = selectMasterWithLowestLoad(masterIds);

        if (masterId == null) {
            serviceRequest.setMasterId(null);
            serviceRequest.setStatus(
                    RequestStatus.WAITING_FOR_ASSIGNMENT
            );
        } else {
            serviceRequest.setMasterId(masterId);
            serviceRequest.setStatus(RequestStatus.ASSIGNED);
        }

        return toResponse(
                serviceRequestRepository.save(serviceRequest)
        );
    }

    /**
     * Подробная карточка заявки.
     *
     * CLIENT: только собственная заявка.
     * ENGINEER: только заявка, назначенная текущему мастеру.
     * DISPATCHER и ADMIN: любая заявка.
     */
    @Transactional(readOnly = true)
    public RequestDetailsResponse getRequestDetails(
            UUID userId,
            String role,
            UUID requestId
    ) {
        ServiceRequest request;

        if ("CLIENT".equals(role)) {
            request = serviceRequestRepository
                    .findByIdAndClientId(requestId, userId)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND,
                            "Заявка не найдена"
                    ));

        } else if ("ENGINEER".equals(role)) {
            request = serviceRequestRepository
                    .findByIdAndMasterId(requestId, userId)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND,
                            "Заявка не найдена или не назначена вам"
                    ));

        } else if ("DISPATCHER".equals(role)
                || "ADMIN".equals(role)) {
            request = serviceRequestRepository
                    .findById(requestId)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND,
                            "Заявка не найдена"
                    ));

        } else {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Недостаточно прав для просмотра заявки"
            );
        }

        AddressResponse address = getAddress(
                request.getAddressId()
        );

        // Квартира берётся из заявки, а не из профиля клиента.
        if (address != null) {
            address = new AddressResponse(
                    address.cityId(),
                    address.cityName(),
                    address.streetId(),
                    address.streetName(),
                    address.houseId(),
                    address.houseNumber(),
                    request.getApartment(),
                    address.zoneId()
            );
        }

        UserEquipmentResponse equipment = getEquipment(
                request.getClientId(),
                request.getEquipmentId()
        );

        MasterResponse master = null;

        if (request.getMasterId() != null) {
            master = masterByIdRequestService.getMasterById(
                    request.getMasterId()
            );
        }

        ProfileResponseEvent clientProfile =
                profileRequestService.getProfile(
                        request.getClientId(),
                        "CLIENT"
                );

        return new RequestDetailsResponse(
                request.getId(),

                request.getAddressId(),
                address,
                request.getApartment(),

                request.getEquipmentId(),
                equipment,

                request.getMasterId(),
                master,

                request.getClientId(),
                clientProfile.getFirstName(),
                clientProfile.getLastName(),
                clientProfile.getMiddleName(),

                request.getProblemDescription(),
                request.getClientComment(),
                request.getStatus(),
                request.getPreferredVisitAt(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }

    /**
     * Получение адреса через существующий Kafka-механизм.
     */
    private AddressResponse getAddress(UUID addressId) {
        if (addressId == null) {
            return null;
        }

        return addressByIdRequestService.getAddressById(addressId);
    }

    /**
     * Получение оборудования через существующий Kafka-механизм.
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
     * Получение кандидатов для ручного назначения мастера.
     */
    @Transactional(readOnly = true)
    public List<AssignmentCandidateResponse> getAssignmentCandidates(
            UUID requestId
    ) {
        ServiceRequest request = serviceRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Заявка не найдена"
                ));

        if (request.getStatus() != RequestStatus.WAITING_FOR_ASSIGNMENT
                && request.getStatus() != RequestStatus.ASSIGNED) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Нельзя назначить мастера на текущем этапе заявки"
            );
        }

        UserEquipmentResponse equipment = getEquipment(
                request.getClientId(),
                request.getEquipmentId()
        );

        if (equipment == null) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Оборудование заявки не найдено"
            );
        }

        List<UUID> masterIds =
                masterAssignmentCandidatesRequestService.getCandidates(
                        null,
                        equipment.getEquipmentTypeId()
                );

        return masterIds.stream()
                .map(masterId -> {
                    MasterResponse master =
                            masterByIdRequestService.getMasterById(masterId);

                    long activeRequests =
                            serviceRequestRepository
                                    .countActiveRequestsByMasterId(masterId);

                    return new AssignmentCandidateResponse(
                            master.id(),
                            master.firstName(),
                            master.lastName(),
                            master.middleName(),
                            master.phone(),
                            activeRequests
                    );
                })
                .toList();
    }

    /**
     * Список заявок клиента.
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
     * Список заявок, назначенных текущему мастеру.
     *
     * В список попадают его заявки, которые назначены,
     * находятся в работе или ожидают последующих действий.
     */
    @Transactional(readOnly = true)
    public List<ManagementRequestResponse> getMasterRequests(UUID masterId) {
        System.out.println("MASTER ID FROM JWT: " + masterId);

        List<ServiceRequest> requests =
                serviceRequestRepository.findAllByMasterId(masterId);

        System.out.println("REQUESTS FOUND BY MASTER ID: " + requests.size());

        requests.forEach(request -> System.out.println(
                "REQUEST ID: " + request.getId()
                        + ", MASTER ID IN DB: " + request.getMasterId()
                        + ", STATUS: " + request.getStatus()
        ));

        return requests.stream()
                .filter(request ->
                        request.getStatus() == RequestStatus.ASSIGNED
                                || request.getStatus() == RequestStatus.IN_PROGRESS
                                || request.getStatus() == RequestStatus.REPORT_SUBMITTED
                                || request.getStatus() == RequestStatus.REVISION_REQUIRED
                )
                .map(request -> new ManagementRequestResponse(
                        request.getId(),
                        request.getCreatedAt(),
                        request.getStatus()
                ))
                .toList();
    }

    /**
     * Принятие заявки мастером.
     *
     * Разрешено только назначенному мастеру
     * и только при статусе ASSIGNED.
     *
     * После принятия статус становится IN_PROGRESS.
     */
    @Transactional
    public RequestResponse acceptAssignedRequest(
            UUID masterId,
            UUID requestId
    ) {
        ServiceRequest request = getRequestForUpdate(requestId);

        checkAssignedMasterAndStatus(request, masterId);

        request.setStatus(RequestStatus.IN_PROGRESS);

        return toResponse(
                serviceRequestRepository.save(request)
        );
    }

    /**
     * Отклонение назначения мастером.
     *
     * Автоматически другого мастера не назначаем.
     * Очищаем masterId и переводим заявку в WAITING_FOR_ASSIGNMENT.
     */
    @Transactional
    public RequestResponse rejectAssignedRequest(
            UUID masterId,
            UUID requestId
    ) {
        ServiceRequest request = getRequestForUpdate(requestId);

        checkAssignedMasterAndStatus(request, masterId);

        request.setMasterId(null);
        request.setStatus(RequestStatus.WAITING_FOR_ASSIGNMENT);

        return toResponse(
                serviceRequestRepository.save(request)
        );
    }

    /**
     * Проверка, что заявка назначена текущему мастеру
     * и всё ещё ожидает его решения.
     */
    private void checkAssignedMasterAndStatus(
            ServiceRequest request,
            UUID masterId
    ) {
        if (masterId == null
                || !masterId.equals(request.getMasterId())) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Заявка не назначена текущему мастеру"
            );
        }

        if (request.getStatus() != RequestStatus.ASSIGNED) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Решение можно принять только по заявке со статусом ASSIGNED"
            );
        }
    }

    /**
     * Редактирование заявки клиентом.
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

        request.setClientComment(
                normalizeComment(update.clientComment())
        );

        request.setPreferredVisitAt(
                update.preferredVisitAt()
        );

        return toResponse(
                serviceRequestRepository.save(request)
        );
    }

    /**
     * Отмена заявки клиентом.
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
     * Получение заявки с блокировкой на изменение.
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
     * Нормализация номера квартиры.
     */
    private String normalizeApartment(String apartment) {
        if (apartment == null || apartment.isBlank()) {
            return null;
        }

        return apartment.trim();
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
     * Выбор мастера с минимальной активной нагрузкой.
     */
    private UUID selectMasterWithLowestLoad(List<UUID> masterIds) {
        if (masterIds == null || masterIds.isEmpty()) {
            return null;
        }

        UUID selectedMasterId = null;
        long minLoad = Long.MAX_VALUE;

        for (UUID masterId : masterIds) {
            long activeRequests =
                    serviceRequestRepository
                            .countActiveRequestsByMasterId(masterId);

            if (activeRequests < minLoad) {
                minLoad = activeRequests;
                selectedMasterId = masterId;
            }
        }

        return selectedMasterId;
    }

    /**
     * Отмена заявки диспетчером или администратором.
     */
    @Transactional
    public RequestResponse cancelRequestByAdmin(UUID requestId) {
        ServiceRequest request = getRequestForUpdate(requestId);

        request.setStatus(RequestStatus.CANCELLED);

        return toResponse(
                serviceRequestRepository.save(request)
        );
    }

    /**
     * Список заявок для диспетчера и администратора.
     */
    @Transactional(readOnly = true)
    public List<ManagementRequestResponse> getManagementRequests() {
        return serviceRequestRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .sorted(
                        Comparator.comparingInt(
                                request -> getManagementPriority(
                                        request.getStatus()
                                )
                        )
                )
                .map(request -> new ManagementRequestResponse(
                        request.getId(),
                        request.getCreatedAt(),
                        request.getStatus()
                ))
                .toList();
    }

    private int getManagementPriority(RequestStatus status) {
        return switch (status) {
            case WAITING_FOR_ASSIGNMENT -> 0;
            case ASSIGNED -> 1;
            case REJECTED -> 2;
            case NEW -> 3;
            case ACCEPTED -> 4;
            case IN_PROGRESS -> 5;
            case REPORT_SUBMITTED -> 6;
            case REVISION_REQUIRED -> 7;
            case COMPLETED -> 8;
            case CANCELLED -> 9;
        };
    }

    /**
     * Ручное назначение или переназначение мастера.
     */
    @Transactional
    public RequestResponse assignMaster(
            UUID requestId,
            UUID masterId
    ) {
        if (masterId == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Необходимо указать мастера"
            );
        }

        ServiceRequest request = getRequestForUpdate(requestId);

        boolean canAssign = switch (request.getStatus()) {
            case WAITING_FOR_ASSIGNMENT, ASSIGNED -> true;
            default -> false;
        };

        if (!canAssign) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Нельзя назначить мастера на текущем этапе заявки"
            );
        }

        UserEquipmentResponse equipment = getEquipment(
                request.getClientId(),
                request.getEquipmentId()
        );

        if (equipment == null) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Оборудование заявки не найдено"
            );
        }

        boolean suitable = masterByIdRequestService.isSuitable(
                masterId,
                equipment.getEquipmentTypeId()
        );

        if (!suitable) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Специализация мастера не соответствует оборудованию заявки"
            );
        }

        request.setMasterId(masterId);
        request.setStatus(RequestStatus.ASSIGNED);

        return toResponse(
                serviceRequestRepository.save(request)
        );
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
