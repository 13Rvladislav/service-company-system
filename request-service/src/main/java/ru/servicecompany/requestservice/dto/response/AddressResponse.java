package ru.servicecompany.requestservice.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AddressResponse(

        UUID cityId,
        String cityName,

        UUID streetId,
        String streetName,

        UUID houseId,
        String houseNumber,

        String apartment

) {
}