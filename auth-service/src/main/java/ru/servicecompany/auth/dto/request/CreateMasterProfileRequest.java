package ru.servicecompany.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateMasterProfileRequest {

    private UUID authUserId;

    private String firstName;
    private String lastName;
    private String middleName;

    private String phone;

    private String employeeNumber;

    private String specialization;

    private UUID zoneId;
}