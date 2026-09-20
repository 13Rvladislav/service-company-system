package ru.servicecompany.auth.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Запрос в user-service на создание профиля.
 */
@Data
@Builder
public class CreateUserProfileRequest {

    private UUID authUserId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;

}