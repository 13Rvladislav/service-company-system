package ru.servicecompany.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.user.dto.request.CreateZoneRequest;
import ru.servicecompany.user.dto.response.ZoneResponse;
import ru.servicecompany.user.service.ZoneService;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;

    /**
     * Создать зону.
     */
    @PostMapping
    public ZoneResponse create(
            @Valid @RequestBody CreateZoneRequest request
    ) {
        return zoneService.create(request);
    }

    /**
     * Получить список зон.
     */
    @GetMapping
    public List<ZoneResponse> getAll() {
        return zoneService.getAll();
    }
}