package ru.servicecompany.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.auth.dto.request.CreateEmployeeRequest;
import ru.servicecompany.auth.dto.response.AdminUserCardResponse;
import ru.servicecompany.auth.dto.response.AdminUserResponse;
import ru.servicecompany.auth.dto.response.CreateEmployeeResponse;
import ru.servicecompany.auth.entity.RoleName;
import ru.servicecompany.auth.service.AuthService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;

    /**
     * Создание сотрудника.
     */
    @PostMapping("/employees")
    public CreateEmployeeResponse createEmployee(
            @RequestBody @Valid CreateEmployeeRequest request
    ) {
        return authService.createEmployee(request);
    }

    /**
     * Получение пользователей по роли.
     */
    @GetMapping("/users")
    public List<AdminUserResponse> getUsers(
            @RequestParam RoleName role
    ) {
        return authService.getUsersByRole(role);
    }

    /**
     * Получение полной карточки пользователя.
     */
    @GetMapping("/users/{id}")
    public AdminUserCardResponse getUserCard(
            @PathVariable UUID id
    ) {
        return authService.getUserCard(id);
    }

    /**
     * Блокировка / разблокировка пользователя.
     * <p>
     * enabled=true  -> разблокировать
     * enabled=false -> заблокировать
     */
    @PatchMapping("/users/{id}/status")
    public AdminUserResponse setUserStatus(
            @PathVariable UUID id,
            @RequestParam boolean enabled
    ) {

        return authService.setUserEnabled(
                id,
                enabled
        );
    }
}