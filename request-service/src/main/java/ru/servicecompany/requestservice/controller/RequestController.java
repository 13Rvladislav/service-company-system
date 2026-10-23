
package ru.servicecompany.requestservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import ru.servicecompany.requestservice.kafka.event.ProfileResponseEvent;
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
     * Получение заявок текущего клиента.
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
     * Получение подробной карточки заявки.
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
     * Получение данных для формы создания заявки.
     */
    @GetMapping("/form-data")
    public CreateRequestFormResponse getCreateForm(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return createRequestFormService.getCreateForm(principal);
    }

    /**
     * Назначение или переназначение мастера.
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
     * Получение кандидатов для ручного назначения мастера.
     */
    @GetMapping("/{id}/assignment-candidates")
    public List<AssignmentCandidateResponse> getAssignmentCandidates(
            @PathVariable UUID id
    ) {
        return requestService.getAssignmentCandidates(id);
    }

    /**
     * Получение списка заявок для диспетчера и администратора.
     */
    @GetMapping("/all")
    public ResponseEntity<List<ManagementRequestResponse>> getManagementRequests() {
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


}
