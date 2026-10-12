
package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateSpecializationRequest {

    @NotBlank(message = "Название специализации обязательно")
    @Size(max = 100, message = "Название не должно превышать 100 символов")
    private String name;

    @NotNull(message = "Необходимо выбрать тип оборудования")
    private UUID equipmentTypeId;
}
