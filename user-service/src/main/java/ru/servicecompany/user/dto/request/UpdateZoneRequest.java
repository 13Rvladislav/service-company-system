package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateZoneRequest {

    @NotBlank(message = "Название зоны обязательно")
    private String name;

    private String description;
}