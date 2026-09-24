package ru.servicecompany.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ZoneResponse {

    private UUID id;

    private String name;

    private String description;
}