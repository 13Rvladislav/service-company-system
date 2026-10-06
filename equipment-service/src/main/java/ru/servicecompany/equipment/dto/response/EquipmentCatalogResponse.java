package ru.servicecompany.equipment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class EquipmentCatalogResponse {

    private UUID id;

    private UUID equipmentTypeId;

    private String equipmentTypeName;

    private String manufacturer;

    private String model;

    private String description;
}