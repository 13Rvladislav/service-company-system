package ru.servicecompany.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.auth.dto.request.CreateEmployeeRequest;
import ru.servicecompany.auth.dto.response.AdminUserResponse;
import ru.servicecompany.auth.dto.response.CreateEmployeeResponse;
import ru.servicecompany.auth.entity.RoleName;
import ru.servicecompany.auth.service.AuthService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;

    /**
     * Создание сотрудника (мастер/диспетчер/администратор).
     */
    @PostMapping("/employees")
    public CreateEmployeeResponse createEmployee(
            @RequestBody @Valid CreateEmployeeRequest request
    ) {
        return authService.createEmployee(request);
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getUsers(
            @RequestParam RoleName role
    ) {
        return authService.getUsersByRole(role);
    }
}