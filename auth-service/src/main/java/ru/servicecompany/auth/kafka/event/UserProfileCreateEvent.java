package ru.servicecompany.auth.kafka.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileCreateEvent {

    private UUID authUserId;

    private String role;

    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;

    // ENGINEER
    private String employeeNumber;
    private String specialization;
    private UUID zoneId;

    // DISPATCHER
    private String department;

    // ADMIN
    private String position;
}