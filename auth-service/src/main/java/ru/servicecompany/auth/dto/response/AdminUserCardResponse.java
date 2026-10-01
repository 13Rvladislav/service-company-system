package ru.servicecompany.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AdminUserCardResponse {

    private UUID id;

    private UUID authUserId;

    private String firstName;
    private String lastName;
    private String middleName;

    private String email;
    private String phone;

    private String role;

    private Boolean enabled;
    private Boolean hasAvatar;

    // CLIENT

    private UUID cityId;
    private UUID streetId;
    private UUID houseId;
    private String apartment;

    // ENGINEER

    private String employeeNumber;
    private String specialization;
    private String status;

    // DISPATCHER

    private String department;

    // ADMIN

    private String position;
}