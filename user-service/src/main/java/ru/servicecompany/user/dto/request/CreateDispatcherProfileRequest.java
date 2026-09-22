package ru.servicecompany.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CreateDispatcherProfileRequest {

    private UUID authUserId;

    private String firstName;
    private String lastName;
    private String middleName;

    private String phone;

    private String employeeNumber;

    private String department;
}