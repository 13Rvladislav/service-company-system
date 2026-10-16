
package ru.servicecompany.requestservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateRequestRequest(

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

        @Future(
                message = "Желаемое время визита должно быть в будущем"
        )
        LocalDateTime preferredVisitAt

) {
}
