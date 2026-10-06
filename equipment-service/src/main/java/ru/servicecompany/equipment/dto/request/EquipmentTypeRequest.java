package ru.servicecompany.equipment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquipmentTypeRequest {

    @NotBlank(message = "Название типа оборудования обязательно")
    @Size(
            max = 100,
            message = "Название типа оборудования не должно превышать 100 символов"
    )
    private String name;
}