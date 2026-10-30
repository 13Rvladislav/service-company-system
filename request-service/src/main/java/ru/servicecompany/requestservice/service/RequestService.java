
package ru.servicecompany.requestservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
    private final RequestStatusTransitionService requestStatusTransitionService;
    private final AuditService auditService;

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
        serviceRequest.setZoneId(address.zoneId());
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

        ServiceRequest savedRequest =
                serviceRequestRepository.save(serviceRequest);

        auditService.logSuccess(
                "REQUEST_CREATED",
                "SERVICE_REQUEST",
                savedRequest.getId(),
                "status=" + savedRequest.getStatus()
                        + "; assignmentMode=AUTO"
        );

        if (masterId != null) {
            auditService.logSystemSuccess(
                    "MASTER_AUTO_ASSIGNED",
                    "SERVICE_REQUEST",
                    savedRequest.getId(),
                    "masterId=" + masterId
                            + "; status=" + savedRequest.getStatus()
            );
        } else {
            auditService.logSystemSuccess(
                    "MASTER_AUTO_ASSIGNMENT_FAILED",
                    "SERVICE_REQUEST",
                    savedRequest.getId(),
                    "reason=NO_AVAILABLE_CANDIDATES"
                            + "; status=" + savedRequest.getStatus()
            );
        }

        log.info(
                "BUSINESS_EVENT event=REQUEST_CREATED requestId={} clientId={} status={}",
                savedRequest.getId(),
                clientId,
                savedRequest.getStatus()
        );

        if (masterId != null) {
            log.info(
                    "BUSINESS_EVENT event=MASTER_AUTO_ASSIGNED requestId={} masterId={} zoneId={}",
                    savedRequest.getId(),
                    masterId,
                    savedRequest.getZoneId()
            );
        } else {
            log.warn(
                    "BUSINESS_EVENT event=MASTER_AUTO_ASSIGNMENT_FAILED requestId={} zoneId={} equipmentTypeId={} reason=NO_AVAILABLE_CANDIDATES",
                    savedRequest.getId(),
                    savedRequest.getZoneId(),
                    equipment.getEquipmentTypeId()
            );
        }

        return toResponse(savedRequest);
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
     */
    @Transactional(readOnly = true)
    public List<ManagementRequestResponse> getMasterRequests(UUID masterId) {
        List<ServiceRequest> requests =
                serviceRequestRepository.findAllByMasterId(masterId);

        List<ManagementRequestResponse> result = requests.stream()
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

        log.debug(
                "Master requests retrieved: masterId={}, total={}, visible={}",
                masterId,
                requests.size(),
                result.size()
        );

        return result;
    }

    /**
     * Принятие заявки мастером.
     *
     * ASSIGNED -> IN_PROGRESS.
     */
    @Transactional
    public RequestResponse acceptAssignedRequest(
            UUID masterId,
            UUID requestId
    ) {
        ServiceRequest request = getRequestForUpdate(requestId);

        checkAssignedMasterAndStatus(request, masterId);

        RequestStatus previousStatus = request.getStatus();

        request.setStatus(
                requestStatusTransitionService.transition(
                        previousStatus,
                        RequestStatus.IN_PROGRESS
                )
        );

        ServiceRequest savedRequest =
                serviceRequestRepository.save(request);

        auditService.logSuccess(
                "MASTER_ACCEPTED_REQUEST",
                "SERVICE_REQUEST",
                savedRequest.getId(),
                "previousStatus=" + previousStatus
                        + "; newStatus=" + savedRequest.getStatus()
        );

        log.info(
                "BUSINESS_EVENT event=MASTER_ACCEPTED_REQUEST requestId={} masterId={} previousStatus={} newStatus={}",
                requestId,
                masterId,
                previousStatus,
                savedRequest.getStatus()
        );

        return toResponse(savedRequest);
    }

    /**
     * Отклонение назначения мастером.
     *
     * ASSIGNED -> WAITING_FOR_ASSIGNMENT.
     * masterId -> null.
     */
    @Transactional
    public RequestResponse rejectAssignedRequest(
            UUID masterId,
            UUID requestId
    ) {
        ServiceRequest request = getRequestForUpdate(requestId);

        checkAssignedMasterAndStatus(request, masterId);

        RequestStatus previousStatus = request.getStatus();

        RequestStatus newStatus =
                requestStatusTransitionService.transition(
                        previousStatus,
                        RequestStatus.WAITING_FOR_ASSIGNMENT
                );

        request.setMasterId(null);
        request.setStatus(newStatus);

        ServiceRequest savedRequest =
                serviceRequestRepository.save(request);

        auditService.logSuccess(
                "MASTER_REJECTED_REQUEST",
                "SERVICE_REQUEST",
                savedRequest.getId(),
                "previousStatus=" + previousStatus
                        + "; newStatus=" + savedRequest.getStatus()
        );

        log.warn(
                "BUSINESS_EVENT event=MASTER_REJECTED_REQUEST requestId={} masterId={} previousStatus={} newStatus={}",
                requestId,
                masterId,
                previousStatus,
                savedRequest.getStatus()
        );

        return toResponse(savedRequest);
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

        ServiceRequest savedRequest =
                serviceRequestRepository.save(request);

        auditService.logSuccess(
                "REQUEST_UPDATED",
                "SERVICE_REQUEST",
                savedRequest.getId(),
                "status=" + savedRequest.getStatus()
        );

        log.info(
                "BUSINESS_EVENT event=REQUEST_UPDATED requestId={} clientId={} status={}",
                requestId,
                clientId,
                savedRequest.getStatus()
        );

        return toResponse(savedRequest);
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

        RequestStatus previousStatus = request.getStatus();

        request.setStatus(
                requestStatusTransitionService.transition(
                        previousStatus,
                        RequestStatus.CANCELLED
                )
        );

        ServiceRequest savedRequest =
                serviceRequestRepository.save(request);

        auditService.logSuccess(
                "REQUEST_CANCELLED",
                "SERVICE_REQUEST",
                savedRequest.getId(),
                "previousStatus=" + previousStatus
                        + "; newStatus=" + savedRequest.getStatus()
                        + "; cancelledBy=CLIENT"
        );

        log.info(
                "BUSINESS_EVENT event=REQUEST_CANCELLED requestId={} clientId={} previousStatus={} newStatus={} cancelledBy=CLIENT",
                requestId,
                clientId,
                previousStatus,
                savedRequest.getStatus()
        );

        return toResponse(savedRequest);
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

        RequestStatus previousStatus = request.getStatus();

        request.setStatus(RequestStatus.CANCELLED);

        ServiceRequest savedRequest =
                serviceRequestRepository.save(request);

        auditService.logSuccess(
                "REQUEST_CANCELLED",
                "SERVICE_REQUEST",
                savedRequest.getId(),
                "previousStatus=" + previousStatus
                        + "; newStatus=" + savedRequest.getStatus()
                        + "; cancelledBy=DISPATCHER_OR_ADMIN"
        );

        log.warn(
                "BUSINESS_EVENT event=REQUEST_CANCELLED requestId={} previousStatus={} newStatus={} cancelledBy=DISPATCHER_OR_ADMIN",
                requestId,
                previousStatus,
                savedRequest.getStatus()
        );

        return toResponse(savedRequest);
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

    /**
     * Приоритет отображения заявок для диспетчера и администратора.
     */
    private int getManagementPriority(RequestStatus status) {
        return switch (status) {
            case WAITING_FOR_ASSIGNMENT -> 0;
            case ASSIGNED -> 1;
            case NEW -> 2;
            case REVISION_REQUIRED -> 3;
            case IN_PROGRESS -> 4;
            case REPORT_SUBMITTED -> 5;
            case COMPLETED -> 6;
            case CANCELLED -> 7;
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

        UUID previousMasterId = request.getMasterId();
        RequestStatus previousStatus = request.getStatus();

        request.setMasterId(masterId);
        request.setStatus(RequestStatus.ASSIGNED);

        ServiceRequest savedRequest =
                serviceRequestRepository.save(request);

        String event = previousMasterId == null
                ? "MASTER_ASSIGNED_MANUALLY"
                : "MASTER_REASSIGNED_MANUALLY";

        auditService.logSuccess(
                event,
                "SERVICE_REQUEST",
                savedRequest.getId(),
                "previousMasterId=" + previousMasterId
                        + "; newMasterId=" + masterId
                        + "; previousStatus=" + previousStatus
                        + "; newStatus=" + savedRequest.getStatus()
        );

        log.info(
                "BUSINESS_EVENT event={} requestId={} previousMasterId={} newMasterId={} previousStatus={} newStatus={}",
                event,
                requestId,
                previousMasterId,
                masterId,
                previousStatus,
                savedRequest.getStatus()
        );

        return toResponse(savedRequest);
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
