package ru.servicecompany.equipment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class EquipmentTypeResponse {

    private UUID id;

    private String name;
}