package ru.servicecompany.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AdminUserResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String middleName;

    private String email;

    private String phone;

    private String role;

    private Boolean enabled;
}