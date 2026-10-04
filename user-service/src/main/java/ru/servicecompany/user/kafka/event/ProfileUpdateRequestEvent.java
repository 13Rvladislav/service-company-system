package ru.servicecompany.user.kafka.event;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
public class ProfileUpdateRequestEvent {

    private UUID requestId;

    private UUID authUserId;

    private String role;

    private String firstName;

    private String lastName;

    private String middleName;

    private String phone;

    /*
     * CLIENT
     */
    private UUID houseId;

    private String apartment;

    /*
     * ENGINEER
     */
    private String employeeNumber;

    private String specialization;

    private UUID zoneId;

    private String status;

    /*
     * DISPATCHER
     */
    private String department;

    /*
     * ADMIN
     */
    private String position;

    public ProfileUpdateRequestEvent(
            UUID requestId,
            UUID authUserId,
            String role,
            String firstName,
            String lastName,
            String middleName,
            String phone,
            UUID houseId,
            String apartment,
            String employeeNumber,
            String specialization,
            UUID zoneId,
            String status,
            String department,
            String position
    ) {
        this.requestId = requestId;
        this.authUserId = authUserId;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.phone = phone;
        this.houseId = houseId;
        this.apartment = apartment;
        this.employeeNumber = employeeNumber;
        this.specialization = specialization;
        this.zoneId = zoneId;
        this.status = status;
        this.department = department;
        this.position = position;
    }
}