package ru.servicecompany.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Ответ с данными профиля.
 */
@Data
@Builder
public class UserProfileResponse {

    private UUID id;
    private UUID authUserId;

    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;
}