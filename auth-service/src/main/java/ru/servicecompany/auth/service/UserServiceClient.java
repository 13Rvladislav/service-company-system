package ru.servicecompany.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.servicecompany.auth.dto.request.CreateUserProfileRequest;

/**
 * Клиент для взаимодействия с user-service.
 */
@Service
@RequiredArgsConstructor
public class UserServiceClient {

    @Qualifier("userRestClient")
    private final RestClient restClient;

    /**
     * Создание профиля пользователя в user-service.
     */
    public void createProfile(CreateUserProfileRequest request) {

        restClient.post()
                .uri("/internal/users")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}