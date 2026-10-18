
package ru.servicecompany.requestservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
import ru.servicecompany.requestservice.dto.request.CreateRequestRequest;
import ru.servicecompany.requestservice.dto.request.UpdateRequestRequest;
import ru.servicecompany.requestservice.dto.response.RequestResponse;
import ru.servicecompany.requestservice.kafka.ProfileRequestService;
import ru.servicecompany.requestservice.kafka.event.ProfileResponseEvent;
import ru.servicecompany.requestservice.service.RequestService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final ProfileRequestService profileRequestService;

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

    @GetMapping("/my")
    public List<RequestResponse> getMyRequests(
            Authentication authentication
    ) {
        JwtUserPrincipal principal =
                (JwtUserPrincipal) authentication.getPrincipal();

        return requestService.getMyRequests(principal.userId());
    }

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

    @GetMapping("/profile")
    public ProfileResponseEvent getClientProfile(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return profileRequestService.getProfile(
                principal.userId(),
                principal.role()
        );
    }

}
