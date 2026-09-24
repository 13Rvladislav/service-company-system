package ru.servicecompany.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserProfileResponse {

    private UUID id;
    private UUID authUserId;

    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;

    private String role;
    private Boolean hasAvatar;

    // Адрес
    private UUID houseId;
    private String city;
    private String street;
    private String house;
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