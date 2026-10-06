package ru.servicecompany.equipment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.equipment.dto.request.EquipmentCatalogRequest;
import ru.servicecompany.equipment.dto.response.EquipmentCatalogResponse;
import ru.servicecompany.equipment.service.EquipmentCatalogService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/equipment/catalog")
@RequiredArgsConstructor
public class EquipmentCatalogController {

    private final EquipmentCatalogService equipmentCatalogService;

    @GetMapping
    public List<EquipmentCatalogResponse> getAll() {

        return equipmentCatalogService.getAll();
    }

    @GetMapping("/{id}")
    public EquipmentCatalogResponse getById(
            @PathVariable UUID id
    ) {

        return equipmentCatalogService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentCatalogResponse create(
            @Valid @RequestBody EquipmentCatalogRequest request
    ) {

        return equipmentCatalogService.create(request);
    }

    @PutMapping("/{id}")
    public EquipmentCatalogResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody EquipmentCatalogRequest request
    ) {

        return equipmentCatalogService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {

        equipmentCatalogService.delete(id);
    }
}