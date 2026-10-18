package ru.servicecompany.requestservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.requestservice.kafka.event.ProfileResponseEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class ProfileResponseConsumer {

    private final ConcurrentMap<UUID, CompletableFuture<ProfileResponseEvent>>
            pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<ProfileResponseEvent> registerRequest(
            UUID requestId
    ) {
        CompletableFuture<ProfileResponseEvent> future =
                new CompletableFuture<>();

        pendingRequests.put(requestId, future);

        log.info(
                "REQUEST SERVICE: зарегистрирован запрос профиля, requestId={}",
                requestId
        );

        return future;
    }

    public void removeRequest(UUID requestId) {
        pendingRequests.remove(requestId);
    }

    @KafkaListener(
            id = "profileResponseListener",
            topics = KafkaTopics.USER_PROFILE_RESPONSE,
            groupId = "request-service-profile-response-group",
            containerFactory = "profileResponseKafkaListenerContainerFactory"
    )
    public void consume(ProfileResponseEvent event) {

        if (event == null) {
            log.error(
                    "REQUEST SERVICE: получен пустой ответ профиля"
            );
            return;
        }

        UUID requestId = event.getRequestId();

        if (requestId == null) {
            log.error(
                    "REQUEST SERVICE: ответ профиля без requestId"
            );
            return;
        }

        log.info(
                "REQUEST SERVICE: получен ответ профиля, requestId={}",
                requestId
        );

        CompletableFuture<ProfileResponseEvent> future =
                pendingRequests.remove(requestId);

        if (future == null) {
            log.warn(
                    "REQUEST SERVICE: нет ожидающего запроса профиля, requestId={}",
                    requestId
            );
            return;
        }

        future.complete(event);

        log.info(
                "REQUEST SERVICE: ответ профиля передан ожидающему запросу, requestId={}",
                requestId
        );
    }
}