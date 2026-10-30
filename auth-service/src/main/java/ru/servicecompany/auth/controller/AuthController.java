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
import ru.servicecompany.auth.enums.AuditResult;
import ru.servicecompany.auth.service.AuditService;
import ru.servicecompany.auth.service.AuthService;
import ru.servicecompany.auth.service.ForgotPasswordService;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ForgotPasswordService forgotPasswordService;
    private final AuditService auditService;

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
     *
     * После успешного входа JWT записывается
     * в HttpOnly Cookie, а не возвращается в JSON.
     */
    @PostMapping("/login")
    public ResponseEntity<MessageResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {

        LoginResponse response = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from(
                        "access_token",
                        response.getToken()
                )
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 24)
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
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            Authentication authentication
    ) {

        if (authentication != null
                && authentication.getPrincipal() instanceof User user) {

            auditService.logAuthEvent(
                    "AUTH_LOGOUT",
                    user.getId(),
                    user.getRole().getName().name(),
                    AuditResult.SUCCESS,
                    "logout=COOKIE_CLEARED"
            );
        }

        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
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