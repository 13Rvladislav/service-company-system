package ru.servicecompany.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.user.dto.request.CreateUserProfileRequest;
import ru.servicecompany.user.dto.response.UserProfileResponse;
import ru.servicecompany.user.service.UserProfileService;

/**
 * Внутренние endpoint'ы.
 * Используются только другими микросервисами.
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserProfileService service;

    /**
     * Создание профиля после регистрации.
     */
    @PostMapping
    public UserProfileResponse create(
            @RequestBody @Valid CreateUserProfileRequest request
    ) {
        return service.create(request);
    }
}