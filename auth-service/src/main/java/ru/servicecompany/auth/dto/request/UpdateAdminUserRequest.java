package ru.servicecompany.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateAdminUserRequest {

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    private String middleName;

    @Email(message = "Некорректный Email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @NotBlank(message = "Телефон обязателен")
    private String phone;

    /**
     * CLIENT
     */
    private UUID houseId;

    private String apartment;

    /**
     * ENGINEER
     */
    private String employeeNumber;

    private String specialization;

    private UUID zoneId;

    private String status;

    /**
     * DISPATCHER
     */
    private String department;

    /**
     * ADMIN
     */
    private String position;
}