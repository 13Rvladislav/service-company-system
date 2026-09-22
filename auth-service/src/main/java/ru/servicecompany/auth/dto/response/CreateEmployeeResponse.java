package ru.servicecompany.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateEmployeeResponse {

    private String email;

    /**
     * Временный пароль,
     * который нужно передать сотруднику.
     */
    private String temporaryPassword;

    private String message;
}