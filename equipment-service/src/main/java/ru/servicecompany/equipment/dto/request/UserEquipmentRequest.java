package ru.servicecompany.equipment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class UserEquipmentRequest {

    @NotNull(message = "Оборудование обязательно")
    private UUID equipmentCatalogId;

    @Size(
            max = 100,
            message = "Серийный номер не должен превышать 100 символов"
    )
    private String serialNumber;

    private LocalDate installationDate;
}