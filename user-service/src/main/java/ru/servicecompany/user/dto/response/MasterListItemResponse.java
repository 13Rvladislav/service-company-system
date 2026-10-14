package ru.servicecompany.user.dto.response;

import java.util.UUID;

public record MasterListItemResponse(
        UUID id,
        String firstName,
        String lastName,
        String middleName,
        String employeeNumber,
        String phone,
        String status
) {
}