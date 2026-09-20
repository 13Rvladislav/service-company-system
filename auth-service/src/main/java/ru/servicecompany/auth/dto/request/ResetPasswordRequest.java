package ru.servicecompany.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Запрос на установку нового пароля.
 */
@Data
public class ResetPasswordRequest {

    // Одноразовый токен из письма
    @NotBlank
    private String token;

    // Новый пароль
    @NotBlank
    @Size(min = 8, message = "Минимум 8 символов")
    private String newPassword;

}