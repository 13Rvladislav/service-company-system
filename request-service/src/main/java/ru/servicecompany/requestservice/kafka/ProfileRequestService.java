package ru.servicecompany.requestservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.common.exception.ApiException;
import ru.servicecompany.requestservice.kafka.event.ProfileResponseEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class ProfileRequestService {

    private final ProfileRequestProducer profileRequestProducer;
    private final ProfileResponseConsumer profileResponseConsumer;

    public ProfileResponseEvent getProfile(
            UUID authUserId,
            String role
    ) {
        UUID requestId = UUID.randomUUID();

        CompletableFuture<ProfileResponseEvent> responseFuture =
                profileResponseConsumer.registerRequest(requestId);

        try {
            profileRequestProducer.requestProfile(
                    requestId,
                    authUserId,
                    role
            );

            return responseFuture.get(5, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            throw new ApiException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "User Service не ответил на запрос профиля"
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ожидание ответа User Service было прервано"
            );

        } catch (Exception e) {
            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Не удалось получить профиль пользователя"
            );

        } finally {
            profileResponseConsumer.removeRequest(requestId);
        }
    }
}