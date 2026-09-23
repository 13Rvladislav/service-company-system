package ru.servicecompany.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UserProfileResponse {

    private UUID id;

    private UUID authUserId;

    private String firstName;

    private String lastName;

    private String middleName;

    private String phone;

    private String city;

    private String street;

    private String house;

    private String apartment;

    private UUID zoneId;

    /**
     * Роль текущего пользователя.
     */
    private String role;

    /**
     * Есть ли фотография профиля.
     */
    private Boolean  hasAvatar;

    /**
     * Поля мастера.
     */
    private String employeeNumber;

    private String specialization;

    private String status;

    /**
     * Поля диспетчера.
     */
    private String department;

    /**
     * Поля администратора.
     */
    private String position;
}