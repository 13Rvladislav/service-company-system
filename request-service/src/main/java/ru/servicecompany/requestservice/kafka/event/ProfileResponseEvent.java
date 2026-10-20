package ru.servicecompany.requestservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseEvent {

    private UUID requestId;

    private UUID authUserId;

    private UUID id;

    private String role;

    private String firstName;

    private String lastName;

    private String middleName;

    private String phone;

    private Boolean hasAvatar;

    // CLIENT

    private UUID cityId;
    private UUID streetId;
    private UUID houseId;
    private UUID zoneId;

    private String cityName;
    private String streetName;
    private String houseNumber;

    private String apartment;

    // MASTER
    private String employeeNumber;
    private String status;
}