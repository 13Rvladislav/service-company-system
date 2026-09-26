package ru.servicecompany.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.user.dto.request.CreateZoneRequest;
import ru.servicecompany.user.dto.request.UpdateZoneRequest;
import ru.servicecompany.user.dto.response.ZoneResponse;
import ru.servicecompany.user.service.ZoneService;

import java.util.List;
import java.util.UUID;

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
            @RequestBody @Valid CreateZoneRequest request
    ) {
        return zoneService.create(request);
    }

    /**
     * Получить все зоны.
     */
    @GetMapping
    public List<ZoneResponse> getAll() {
        return zoneService.getAll();
    }

    /**
     * Обновить название и описание зоны.
     */
    @PutMapping("/{id}")
    public ZoneResponse update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateZoneRequest request
    ) {
        return zoneService.update(id, request);
    }

    /**
     * Удалить зону.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        zoneService.delete(id);
    }
}