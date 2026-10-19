package ru.servicecompany.requestservice.dto.response;

import ru.servicecompany.requestservice.enums.RequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MyRequestResponse(
        UUID id,
        LocalDateTime createdAt,
        RequestStatus status
) {
}