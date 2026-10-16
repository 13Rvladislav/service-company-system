
package ru.servicecompany.requestservice.dto.response;

import ru.servicecompany.requestservice.enums.RequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Данные заявки, возвращаемые клиенту.
 */
public record RequestResponse(

        UUID id,

        UUID clientId,

        UUID masterId,

        UUID addressId,

        UUID equipmentId,

        String problemDescription,

        String clientComment,

        RequestStatus status,

        LocalDateTime preferredVisitAt,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
