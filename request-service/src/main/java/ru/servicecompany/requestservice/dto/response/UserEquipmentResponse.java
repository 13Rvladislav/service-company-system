package ru.servicecompany.requestservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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