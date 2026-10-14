package ru.servicecompany.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
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
    private String email;

    private String role;
    private Boolean hasAvatar;

    // Адрес (ID для каскадных списков)
    private UUID cityId;
    private UUID streetId;
    private UUID houseId;
    private String apartment;

    // ENGINEER
    private String employeeNumber;
    private List<SpecializationResponse> specializations;
    private String status;
    private List<ZoneResponse> zones;

    // DISPATCHER
    private String department;

    // ADMIN
    private String position;
}
