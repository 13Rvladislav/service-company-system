package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateAdminProfileRequest {

    private UUID authUserId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String middleName;

    @NotBlank
    private String phone;

    @NotBlank
    private String employeeNumber;

    @NotBlank
    private String position;
}