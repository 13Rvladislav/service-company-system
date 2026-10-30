
package ru.servicecompany.requestservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.servicecompany.requestservice.config.JwtUserPrincipal;
import ru.servicecompany.requestservice.dto.request.AssignMasterRequest;
import ru.servicecompany.requestservice.dto.request.CreateRequestRequest;
import ru.servicecompany.requestservice.dto.request.UpdateRequestRequest;
import ru.servicecompany.requestservice.dto.response.*;
import ru.servicecompany.requestservice.kafka.ProfileRequestService;
import ru.servicecompany.requestservice.service.CreateRequestFormService;
import ru.servicecompany.requestservice.service.RequestService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final ProfileRequestService profileRequestService;
    private final CreateRequestFormService createRequestFormService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponse createRequest(
            Authentication authentication,
            @Valid @RequestBody CreateRequestRequest request
    ) {
        JwtUserPrincipal principal =
                (JwtUserPrincipal) authentication.getPrincipal();

        return requestService.createRequest(
                principal.userId(),
                request
        );
    }

    /**
     * Заявки текущего клиента.
     */
    @GetMapping("/my")
    public ResponseEntity<List<MyRequestResponse>> getMyRequests(
            Authentication authentication
    ) {
        JwtUserPrincipal principal =
                (JwtUserPrincipal) authentication.getPrincipal();

        return ResponseEntity.ok(
                requestService.getMyRequests(principal.userId())
        );
    }

    /**
     * Подробная карточка заявки.
     */
    @GetMapping("/{id}")
    public RequestDetailsResponse getRequestDetails(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID id
    ) {
        return requestService.getRequestDetails(
                principal.userId(),
                principal.role(),
                id
        );
    }

    /**
     * Редактирование заявки клиентом.
     */
    @PatchMapping("/{id}")
    public RequestResponse updateRequest(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRequestRequest request
    ) {
        JwtUserPrincipal principal =
                (JwtUserPrincipal) authentication.getPrincipal();

        return requestService.updateRequest(
                principal.userId(),
                id,
                request
        );
    }

    /**
     * Отмена заявки клиентом.
     */
    @PostMapping("/{id}/cancel")
    public RequestResponse cancelRequest(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        JwtUserPrincipal principal =
                (JwtUserPrincipal) authentication.getPrincipal();

        return requestService.cancelRequest(
                principal.userId(),
                id
        );
    }

    /**
     * Данные для формы создания заявки.
     */
    @GetMapping("/create-form")
    public CreateRequestFormResponse getCreateForm(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return createRequestFormService.getCreateForm(principal);
    }

    /**
     * Ручное назначение или переназначение мастера.
     */
    @PostMapping("/{id}/assign")
    public RequestResponse assignMaster(
            @PathVariable UUID id,
            @Valid @RequestBody AssignMasterRequest request
    ) {
        return requestService.assignMaster(
                id,
                request.masterId()
        );
    }

    /**
     * Кандидаты для ручного назначения.
     */
    @GetMapping("/{id}/assignment-candidates")
    public List<AssignmentCandidateResponse> getAssignmentCandidates(
            @PathVariable UUID id
    ) {
        return requestService.getAssignmentCandidates(id);
    }

    /**
     * Все заявки для диспетчера и администратора.
     */
    @GetMapping("/all")
    public ResponseEntity<List<ManagementRequestResponse>>
    getManagementRequests() {
        return ResponseEntity.ok(
                requestService.getManagementRequests()
        );
    }

    /**
     * Отмена заявки диспетчером или администратором.
     */
    @PostMapping("/{id}/admin-cancel")
    public RequestResponse cancelRequestByAdmin(
            @PathVariable UUID id
    ) {
        return requestService.cancelRequestByAdmin(id);
    }

    /**
     * Список заявок, назначенных текущему мастеру.
     */
    @GetMapping("/master")
    public List<ManagementRequestResponse> getMasterRequests(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        requireEngineer(principal);

        return requestService.getMasterRequests(
                principal.userId()
        );
    }

    /**
     * Принять назначенную заявку.
     * ASSIGNED -> IN_PROGRESS.
     */
    @PostMapping("/{id}/master-accept")
    public RequestResponse acceptAssignedRequest(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID id
    ) {
        requireEngineer(principal);

        return requestService.acceptAssignedRequest(
                principal.userId(),
                id
        );
    }

    /**
     * Отклонить назначение.
     * ASSIGNED -> WAITING_FOR_ASSIGNMENT, masterId -> null.
     */
    @PostMapping("/{id}/master-reject")
    public RequestResponse rejectAssignedRequest(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID id
    ) {
        requireEngineer(principal);

        return requestService.rejectAssignedRequest(
                principal.userId(),
                id
        );
    }

    private void requireEngineer(JwtUserPrincipal principal) {
        if (principal == null || !"ENGINEER".equals(principal.role())) {
            throw new AccessDeniedException(
                    "Действие доступно только мастеру"
            );
        }
    }
}
