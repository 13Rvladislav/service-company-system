package ru.servicecompany.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdminProfileRequest {

    private UUID authUserId;

    private String firstName;

    private String lastName;

    private String middleName;

    private String phone;

    private String employeeNumber;

    private String position;
}