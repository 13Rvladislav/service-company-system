package ru.servicecompany.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import ru.servicecompany.auth.entity.RoleName;

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
    private String specialization;
    private String zoneId;
    private String position;

    @NotNull
    private RoleName role;
}