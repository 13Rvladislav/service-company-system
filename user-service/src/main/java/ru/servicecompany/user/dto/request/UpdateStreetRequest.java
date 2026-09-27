package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateStreetRequest {

    @NotBlank(message = "Название улицы обязательно")
    private String name;

    @NotNull(message = "Зона обязательна")
    private UUID zoneId;
}