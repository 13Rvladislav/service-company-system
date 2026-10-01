package ru.servicecompany.auth.kafka.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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