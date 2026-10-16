
package ru.servicecompany.requestservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Данные, которые клиент передаёт при создании заявки.
 */
public record CreateRequestRequest(

        @NotNull(message = "Необходимо указать адрес обслуживания")
        UUID addressId,

        @NotNull(message = "Необходимо выбрать оборудование")
        UUID equipmentId,

        @NotBlank(message = "Опишите проблему")
        @Size(
                max = 5000,
                message = "Описание проблемы не должно превышать 5000 символов"
        )
        String problemDescription,

        @Size(
                max = 2000,
                message = "Комментарий не должен превышать 2000 символов"
        )
        String clientComment,

        @Future(message = "Желаемое время визита должно быть в будущем")
        LocalDateTime preferredVisitAt

) {
}
