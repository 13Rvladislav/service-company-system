package ru.servicecompany.equipment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EquipmentCatalogRequest {

    @NotNull(message = "Тип оборудования обязателен")
    private UUID equipmentTypeId;

    @NotBlank(message = "Производитель обязателен")
    @Size(
            max = 100,
            message = "Производитель не должен превышать 100 символов"
    )
    private String manufacturer;

    @NotBlank(message = "Модель обязательна")
    @Size(
            max = 100,
            message = "Модель не должна превышать 100 символов"
    )
    private String model;

    private String description;
}