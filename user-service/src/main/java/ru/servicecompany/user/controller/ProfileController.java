package ru.servicecompany.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.servicecompany.user.config.JwtUserPrincipal;
import ru.servicecompany.user.dto.request.UpdateUserProfileRequest;
import ru.servicecompany.user.dto.response.UserProfileResponse;
import ru.servicecompany.user.service.UserProfileService;

import java.io.IOException;

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

        return userProfileService.getCurrentProfile(principal);
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

    /**
     * Загрузить или заменить фотографию профиля.
     */
    @PostMapping(
            value = "/me/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> uploadAvatar(
            Authentication authentication,
            @RequestPart("file") MultipartFile file
    ) throws IOException {

        JwtUserPrincipal principal =
                (JwtUserPrincipal) authentication.getPrincipal();

        userProfileService.uploadAvatar(
                principal.userId(),
                principal.role(),
                file
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Получить фотографию текущего пользователя.
     */
    @GetMapping(
            value = "/me/avatar",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public byte[] getAvatar(Authentication authentication) {

        JwtUserPrincipal principal =
                (JwtUserPrincipal) authentication.getPrincipal();

        return userProfileService.getAvatar(
                principal.userId(),
                principal.role()
        );
    }
}