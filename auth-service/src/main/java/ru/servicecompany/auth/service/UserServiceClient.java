package ru.servicecompany.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.servicecompany.auth.dto.request.CreateAdminProfileRequest;
import ru.servicecompany.auth.dto.request.CreateDispatcherProfileRequest;
import ru.servicecompany.auth.dto.request.CreateMasterProfileRequest;
import ru.servicecompany.auth.dto.request.CreateUserProfileRequest;

@Service
@RequiredArgsConstructor
public class UserServiceClient {

    @Qualifier("userRestClient")
    private final RestClient restClient;

    /**
     * Клиент.
     */
    public void createProfile(CreateUserProfileRequest request) {

        restClient.post()
                .uri("/internal/users")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Мастер.
     */
    public void createMasterProfile(CreateMasterProfileRequest request) {

        restClient.post()
                .uri("/internal/profiles/master")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Диспетчер.
     */
    public void createDispatcherProfile(CreateDispatcherProfileRequest request) {

        restClient.post()
                .uri("/internal/profiles/dispatcher")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Администратор.
     */
    public void createAdminProfile(CreateAdminProfileRequest request) {

        restClient.post()
                .uri("/internal/profiles/admin")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}