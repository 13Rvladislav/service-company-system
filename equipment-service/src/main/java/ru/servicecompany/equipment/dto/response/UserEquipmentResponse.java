package ru.servicecompany.equipment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class UserEquipmentResponse {

    private UUID id;

    private UUID equipmentCatalogId;

    private UUID equipmentTypeId;

    private String equipmentTypeName;

    private String manufacturer;

    private String model;

    private String description;

    private String serialNumber;

    private LocalDate installationDate;
}