package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCityRequest {

    @NotBlank(message = "Название города обязательно")
    private String name;

}