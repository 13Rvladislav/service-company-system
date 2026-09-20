package ru.servicecompany.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.servicecompany.user.config.JwtUserPrincipal;
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

        return userProfileService.getCurrentProfile(principal.userId());
    }
}