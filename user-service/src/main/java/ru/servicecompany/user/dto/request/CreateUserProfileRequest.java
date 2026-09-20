package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Запрос на создание профиля.
 * Используется только между микросервисами.
 */
@Data
public class CreateUserProfileRequest {

    /** UUID пользователя из auth-service */
    @NotNull
    private UUID authUserId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String middleName;

    @NotBlank
    private String phone;
}