package ru.servicecompany.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateDispatcherProfileRequest {

    private UUID authUserId;

    private String firstName;
    private String lastName;
    private String middleName;

    private String phone;

    private String employeeNumber;

    private String department;
}