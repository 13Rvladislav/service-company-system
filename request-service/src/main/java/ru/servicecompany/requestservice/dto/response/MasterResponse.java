package ru.servicecompany.requestservice.dto.response;

import java.util.UUID;

public record MasterResponse(
        UUID id,
        String firstName,
        String lastName,
        String middleName,
        String phone
) {
}