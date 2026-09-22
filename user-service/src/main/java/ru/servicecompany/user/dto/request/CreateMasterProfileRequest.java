package ru.servicecompany.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
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