package ru.servicecompany.requestservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressByIdResponseEvent {

    private UUID requestId;

    private UUID addressId;

    private UUID cityId;
    private String cityName;

    private UUID streetId;
    private String streetName;

    private UUID houseId;
    private String houseNumber;

    private UUID zoneId;

    private String error;
}