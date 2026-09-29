package ru.servicecompany.user.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileCreateEvent {

    private UUID authUserId;

    private String role;

    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;

    // Только для сотрудников
    private String employeeNumber;
    private String specialization;
    private UUID zoneId;
    private String department;
    private String position;
}