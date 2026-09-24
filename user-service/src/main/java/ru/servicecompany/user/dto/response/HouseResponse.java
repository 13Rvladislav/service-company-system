package ru.servicecompany.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class HouseResponse {

    private UUID id;

    private String number;

    private UUID streetId;

    private String streetName;

}