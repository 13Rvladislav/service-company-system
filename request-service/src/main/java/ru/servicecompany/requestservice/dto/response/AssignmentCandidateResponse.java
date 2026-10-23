package ru.servicecompany.requestservice.dto.response;

import java.util.UUID;

public record AssignmentCandidateResponse(
        UUID masterId,
        String firstName,
        String lastName,
        String middleName,
        String phone,
        long activeRequestsCount
) {
}