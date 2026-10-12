
package ru.servicecompany.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.user.dto.request.AssignSpecializationsRequest;
import ru.servicecompany.user.dto.response.SpecializationResponse;
import ru.servicecompany.user.service.MasterSpecializationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/masters/{masterId}/specializations")
@RequiredArgsConstructor
public class MasterSpecializationAssignmentController {

    private final MasterSpecializationService service;

    @GetMapping
    public List<SpecializationResponse> getMasterSpecializations(
            @PathVariable UUID masterId
    ) {
        return service.getMasterSpecializations(masterId);
    }

    @PutMapping
    public List<SpecializationResponse> assignSpecializations(
            @PathVariable UUID masterId,
            @RequestBody @Valid AssignSpecializationsRequest request
    ) {
        return service.assignToMaster(masterId, request);
    }
}
