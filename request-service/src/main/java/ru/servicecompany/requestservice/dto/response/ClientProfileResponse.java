package ru.servicecompany.requestservice.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ClientProfileResponse(

        UUID id,
        UUID authUserId,

        String role,

        String firstName,
        String lastName,
        String middleName,
        String phone,
        Boolean hasAvatar,

        UUID cityId,
        String cityName,

        UUID streetId,
        String streetName,

        UUID houseId,
        String houseNumber,

        String apartment,

        String employeeNumber,
        String status

) {
}