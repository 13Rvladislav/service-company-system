package ru.servicecompany.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserProfileResponse {

    private UUID id;
    private UUID authUserId;

    // Общие
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String phone;

    private String role;
    private Boolean hasAvatar;

    // Адрес клиента (всегда возвращается)
    private UUID houseId;
    private String city;
    private String street;
    private String house;
    private String apartment;

    // Только мастер
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String employeeNumber;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String specialization;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String status;

    // Только диспетчер
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String department;

    // Только админ
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String position;
}