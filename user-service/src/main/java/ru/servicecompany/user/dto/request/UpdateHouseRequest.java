package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateHouseRequest {

    @NotBlank(message = "Номер дома обязателен")
    private String number;
}