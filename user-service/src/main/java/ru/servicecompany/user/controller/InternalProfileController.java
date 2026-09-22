package ru.servicecompany.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.user.dto.request.CreateAdminProfileRequest;
import ru.servicecompany.user.dto.request.CreateDispatcherProfileRequest;
import ru.servicecompany.user.dto.request.CreateMasterProfileRequest;
import ru.servicecompany.user.service.InternalProfileService;

@RestController
@RequestMapping("/internal/profiles")
@RequiredArgsConstructor
public class InternalProfileController {

    private final InternalProfileService service;

    /**
     * Создание профиля мастера.
     */
    @PostMapping("/master")
    public void createMaster(
            @RequestBody @Valid CreateMasterProfileRequest request
    ) {
        service.createMaster(request);
    }

    /**
     * Создание профиля диспетчера.
     */
    @PostMapping("/dispatcher")
    public void createDispatcher(
            @RequestBody @Valid CreateDispatcherProfileRequest request
    ) {
        service.createDispatcher(request);
    }

    /**
     * Создание профиля администратора.
     */
    @PostMapping("/admin")
    public void createAdmin(
            @RequestBody @Valid CreateAdminProfileRequest request
    ) {
        service.createAdmin(request);
    }
}