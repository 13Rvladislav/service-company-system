package ru.servicecompany.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StreetResponse {

    private UUID id;

    private String name;

    private UUID cityId;

    private String cityName;

    private UUID zoneId;

    private String zoneName;

}