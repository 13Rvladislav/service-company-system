
package ru.servicecompany.requestservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.servicecompany.requestservice.config.JwtUserPrincipal;
import ru.servicecompany.requestservice.dto.request.CreateRequestRequest;
import ru.servicecompany.requestservice.dto.response.RequestResponse;
import ru.servicecompany.requestservice.service.RequestService;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    /**
     * Создаёт заявку от имени авторизованного клиента.
     */
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
}
