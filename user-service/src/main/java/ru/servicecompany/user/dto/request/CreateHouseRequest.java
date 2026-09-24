package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateHouseRequest {

    @NotBlank(message = "Номер дома обязателен")
    private String number;

    @NotNull(message = "Улица обязательна")
    private UUID streetId;

}