package ru.servicecompany.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import ru.servicecompany.auth.entity.RoleName;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateEmployeeRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String middleName;

    @NotBlank
    private String phone;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String employeeNumber;

    private String department;

    // Старое поле временно оставляем для совместимости.
    private String specialization;

    // Новое поле: UUID специализаций из справочника user-service.
    private List<UUID> specializationIds;

    private String zoneId;

    private String position;

    @NotNull
    private RoleName role;
}