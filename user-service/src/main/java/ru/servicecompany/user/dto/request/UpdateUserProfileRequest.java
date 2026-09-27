package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateUserProfileRequest {

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    private String middleName;

    @NotBlank(message = "Телефон обязателен")
    private String phone;

    /**
     * Только для CLIENT.
     * Для остальных ролей может быть null.
     */
    private UUID houseId;

    /**
     * Только для CLIENT.
     */
    private String apartment;
}