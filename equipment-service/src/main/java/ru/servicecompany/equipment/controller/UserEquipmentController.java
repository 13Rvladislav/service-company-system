package ru.servicecompany.equipment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.equipment.config.JwtUserPrincipal;
import ru.servicecompany.equipment.dto.request.UserEquipmentRequest;
import ru.servicecompany.equipment.dto.response.UserEquipmentResponse;
import ru.servicecompany.equipment.service.UserEquipmentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/equipment/my")
@RequiredArgsConstructor
public class UserEquipmentController {

    private final UserEquipmentService userEquipmentService;

    @GetMapping
    public List<UserEquipmentResponse> getMyEquipment(
            Authentication authentication
    ) {
        UUID authUserId = getUserId(authentication);

        return userEquipmentService.getMyEquipment(
                authUserId
        );
    }

    @GetMapping("/{id}")
    public UserEquipmentResponse getMyEquipmentById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID authUserId = getUserId(authentication);

        return userEquipmentService.getMyEquipmentById(
                authUserId,
                id
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserEquipmentResponse addEquipment(
            @Valid @RequestBody UserEquipmentRequest request,
            Authentication authentication
    ) {
        UUID authUserId = getUserId(authentication);

        return userEquipmentService.addEquipment(
                authUserId,
                request
        );
    }

    @PutMapping("/{id}")
    public UserEquipmentResponse updateEquipment(
            @PathVariable UUID id,
            @Valid @RequestBody UserEquipmentRequest request,
            Authentication authentication
    ) {
        UUID authUserId = getUserId(authentication);

        return userEquipmentService.updateEquipment(
                authUserId,
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEquipment(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID authUserId = getUserId(authentication);

        userEquipmentService.deleteEquipment(
                authUserId,
                id
        );
    }

    private UUID getUserId(
            Authentication authentication
    ) {
        JwtUserPrincipal principal =
                (JwtUserPrincipal) authentication.getPrincipal();

        return principal.userId();
    }
}