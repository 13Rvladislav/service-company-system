
package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateSpecializationRequest {

    @NotBlank(message = "Название специализации обязательно")
    @Size(max = 100, message = "Название не должно превышать 100 символов")
    private String name;

    @NotEmpty(message = "Выберите хотя бы один тип оборудования")
    private List<UUID> equipmentTypeIds;
}
