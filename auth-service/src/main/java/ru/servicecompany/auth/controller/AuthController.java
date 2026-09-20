package ru.servicecompany.auth.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.auth.common.exception.ApiException;
import ru.servicecompany.auth.dto.request.ForgotPasswordRequest;
import ru.servicecompany.auth.dto.request.LoginRequest;
import ru.servicecompany.auth.dto.request.RegisterRequest;
import ru.servicecompany.auth.dto.request.ResetPasswordRequest;
import ru.servicecompany.auth.dto.response.LoginResponse;
import ru.servicecompany.auth.dto.response.MessageResponse;
import ru.servicecompany.auth.dto.response.UserResponse;
import ru.servicecompany.auth.entity.User;
import ru.servicecompany.auth.service.AuthService;
import ru.servicecompany.auth.service.ForgotPasswordService;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Бизнес-логика авторизации
    private final AuthService authService;
    private final ForgotPasswordService forgotPasswordService;

    /**
     * Регистрация нового пользователя.
     */
    @PostMapping("/register")
    public UserResponse register(
            @RequestBody @Valid RegisterRequest request
    ) {
        return authService.register(request);
    }

    /**
     * Авторизация пользователя.
     * <p>
     * После успешного входа JWT записывается
     * в HttpOnly Cookie, а не возвращается в JSON.
     */
    @PostMapping("/login")

    public ResponseEntity<MessageResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {

        // Получаем JWT из сервиса
        LoginResponse response = authService.login(request);

        // Создаём защищённую Cookie
        ResponseCookie cookie = ResponseCookie.from(
                        "access_token",
                        response.getToken()
                )
                .httpOnly(true)     // JavaScript не видит токен
                .secure(false)      // В production -> true
                .path("/")
                .maxAge(60 * 60 * 24) // 24 часа
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("Авторизация выполнена успешно"));
    }

    /**
     * Информация о текущем пользователе.
     */
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {

        if (authentication == null) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Пользователь не авторизован"
            );
        }

        User user = (User) authentication.getPrincipal();

        return authService.getCurrentUser(user);
    }

    /**
     * Выход из системы.
     * <p>
     * Пока просто удаляет JWT Cookie.
     * Позже здесь будет удаление Refresh Token.
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {

        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)      // В production -> true
                .path("/")
                .maxAge(0)          // Удалить cookie
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("Вы успешно вышли из системы"));
    }

    /**
     * Запросить восстановление пароля.
     */
    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request
    ) {
        return forgotPasswordService.forgotPassword(request);
    }

    /**
     * Установить новый пароль по токену.
     */
    @PostMapping("/reset-password")
    public MessageResponse resetPassword(
            @RequestBody @Valid ResetPasswordRequest request
    ) {
        return forgotPasswordService.resetPassword(request);
    }
}