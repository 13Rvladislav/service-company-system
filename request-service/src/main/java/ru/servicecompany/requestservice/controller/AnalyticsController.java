package ru.servicecompany.requestservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.servicecompany.requestservice.config.JwtUserPrincipal;
import ru.servicecompany.requestservice.dto.response.analytics.AnalyticsResponse;
import ru.servicecompany.requestservice.service.AnalyticsService;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public AnalyticsResponse getAnalytics(
            @AuthenticationPrincipal JwtUserPrincipal principal,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @RequestParam(required = false)
            UUID zoneId,

            @RequestParam(required = false)
            UUID masterId
    ) {
        if (principal == null) {
            throw new AccessDeniedException("Необходимо авторизоваться");
        }

        if (!"ADMIN".equals(principal.role())
                && !"DISPATCHER".equals(principal.role())) {
            throw new AccessDeniedException(
                    "Аналитика доступна только диспетчеру и администратору"
            );
        }

        return analyticsService.getAnalytics(
                from,
                to,
                zoneId,
                masterId
        );
    }
}