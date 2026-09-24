package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateStreetRequest {

    @NotBlank(message = "Название улицы обязательно")
    private String name;

    @NotNull(message = "Город обязателен")
    private UUID cityId;

    @NotNull(message = "Зона обязательна")
    private UUID zoneId;

}