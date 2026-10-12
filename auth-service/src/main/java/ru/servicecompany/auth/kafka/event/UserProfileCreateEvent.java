package ru.servicecompany.auth.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
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

    // Сотрудники
    private String employeeNumber;
    private String specialization;
    private List<UUID> specializationIds;
    private UUID zoneId;
    private String department;
    private String position;
}