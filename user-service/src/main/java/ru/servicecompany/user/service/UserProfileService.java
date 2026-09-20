package ru.servicecompany.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.servicecompany.user.common.exception.ApiException;
import ru.servicecompany.user.dto.request.CreateUserProfileRequest;
import ru.servicecompany.user.dto.response.UserProfileResponse;
import ru.servicecompany.user.entity.UserProfile;
import ru.servicecompany.user.repository.UserProfileRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;

    /**
     * Создание профиля.
     */
    public UserProfileResponse create(CreateUserProfileRequest request) {

        if (repository.existsByAuthUserId(request.getAuthUserId())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Профиль уже существует"
            );
        }

        UserProfile profile = UserProfile.builder()
                .authUserId(request.getAuthUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .middleName(request.getMiddleName())
                .phone(request.getPhone())
                .build();

        UserProfile saved = repository.save(profile);

        return map(saved);
    }

    /**
     * Получить профиль текущего пользователя.
     */
    public UserProfileResponse getCurrentProfile(UUID userId) {

        UserProfile profile = repository.findByAuthUserId(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Профиль пользователя не найден"
                ));

        return map(profile);
    }

    /**
     * Преобразование Entity → Response DTO.
     */
    private UserProfileResponse map(UserProfile profile) {
        return UserProfileResponse.builder()
                .id(profile.getId())
                .authUserId(profile.getAuthUserId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .middleName(profile.getMiddleName())
                .phone(profile.getPhone())
                .build();
    }
}