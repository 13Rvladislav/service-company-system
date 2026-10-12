package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateMasterProfileRequest {

    @NotNull(message = "ID пользователя обязателен")
    private UUID authUserId;

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    private String middleName;

    @NotBlank(message = "Телефон обязателен")
    private String phone;

    @NotBlank(message = "Табельный номер обязателен")
    @Size(max = 50, message = "Табельный номер не должен превышать 50 символов")
    private String employeeNumber;

    private UUID zoneId;

    private List<UUID> specializationIds;
}
