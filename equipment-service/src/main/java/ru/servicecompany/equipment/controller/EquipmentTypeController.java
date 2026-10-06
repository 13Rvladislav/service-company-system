package ru.servicecompany.equipment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.equipment.dto.request.EquipmentTypeRequest;
import ru.servicecompany.equipment.dto.response.EquipmentTypeResponse;
import ru.servicecompany.equipment.service.EquipmentTypeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/equipment/types")
@RequiredArgsConstructor
public class EquipmentTypeController {

    private final EquipmentTypeService equipmentTypeService;

    @GetMapping
    public List<EquipmentTypeResponse> getAll() {

        return equipmentTypeService.getAll();
    }

    @GetMapping("/{id}")
    public EquipmentTypeResponse getById(
            @PathVariable UUID id
    ) {

        return equipmentTypeService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentTypeResponse create(
            @Valid @RequestBody EquipmentTypeRequest request
    ) {

        return equipmentTypeService.create(request);
    }

    @PutMapping("/{id}")
    public EquipmentTypeResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody EquipmentTypeRequest request
    ) {

        return equipmentTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {

        equipmentTypeService.delete(id);
    }
}