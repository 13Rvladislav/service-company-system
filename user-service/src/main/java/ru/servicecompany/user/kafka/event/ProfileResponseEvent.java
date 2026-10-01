package ru.servicecompany.user.kafka.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseEvent {

    /**
     * Идентификатор исходного запроса.
     */
    private UUID requestId;

    /**
     * ID пользователя из auth-service.
     */
    private UUID authUserId;

    /**
     * ID профильной записи.
     */
    private UUID id;

    /**
     * Роль пользователя.
     */
    private String role;

    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;

    /**
     * Есть ли аватар.
     */
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