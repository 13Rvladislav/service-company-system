
package ru.servicecompany.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.user.dto.request.CreateSpecializationRequest;
import ru.servicecompany.user.dto.request.UpdateSpecializationRequest;
import ru.servicecompany.user.dto.response.SpecializationResponse;
import ru.servicecompany.user.service.MasterSpecializationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/specializations")
@RequiredArgsConstructor
public class MasterSpecializationController {

    private final MasterSpecializationService service;

    @PostMapping
    public SpecializationResponse create(
            @RequestBody @Valid CreateSpecializationRequest request
    ) {
        return service.create(request);
    }

    @GetMapping
    public List<SpecializationResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public SpecializationResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @GetMapping("/equipment-type/{equipmentTypeId}")
    public List<SpecializationResponse> getByEquipmentType(
            @PathVariable UUID equipmentTypeId
    ) {
        return service.getByEquipmentType(equipmentTypeId);
    }

    @PutMapping("/{id}")
    public SpecializationResponse update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateSpecializationRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
