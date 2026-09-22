package ru.servicecompany.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.user.config.JwtUserPrincipal;
import ru.servicecompany.user.dto.request.UpdateUserProfileRequest;
import ru.servicecompany.user.dto.response.UserProfileResponse;
import ru.servicecompany.user.service.UserProfileService;

/**
 * Работа с профилем текущего пользователя.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    /**
     * Получить профиль текущего пользователя.
     */
    @GetMapping("/me")
    public UserProfileResponse me(Authentication authentication) {

        JwtUserPrincipal principal =
                (JwtUserPrincipal) authentication.getPrincipal();

        return userProfileService.getCurrentProfile(
                principal.userId(),
                principal.role()
        );
    }

    /**
     * Обновить профиль текущего пользователя.
     */
    @PutMapping("/me")
    public UserProfileResponse updateMe(
            Authentication authentication,
            @RequestBody @Valid UpdateUserProfileRequest request
    ) {

        JwtUserPrincipal principal =
                (JwtUserPrincipal) authentication.getPrincipal();

        return userProfileService.updateCurrentProfile(
                principal.userId(),
                request
        );
    }
}