package ru.servicecompany.requestservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateRequestRequest(

        @NotNull(message = "Необходимо выбрать адрес")
        UUID addressId,

        @NotNull(message = "Необходимо выбрать оборудование")
        UUID equipmentId,

        @Size(max = 50, message = "Номер квартиры не должен превышать 50 символов")
        String apartment,

        @NotNull(message = "Необходимо указать описание проблемы")
        @Size(
                min = 5,
                max = 2000,
                message = "Описание проблемы должно содержать от 5 до 2000 символов"
        )
        String problemDescription,

        @Size(
                max = 2000,
                message = "Комментарий не должен превышать 2000 символов"
        )
        String clientComment,

        LocalDateTime preferredVisitAt

) {
}