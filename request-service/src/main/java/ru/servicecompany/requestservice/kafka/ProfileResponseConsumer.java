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

    public CompletableFuture<ProfileResponseEvent> registerRequest(UUID requestId) {
        CompletableFuture<ProfileResponseEvent> future = new CompletableFuture<>();

        pendingRequests.put(requestId, future);

        log.info("REQUEST SERVICE: зарегистрирован ожидающий запрос {}", requestId);

        return future;
    }

    public void removeRequest(UUID requestId) {
        pendingRequests.remove(requestId);
    }

    @KafkaListener(
            id = "profileResponseListener",
            topics = KafkaTopics.USER_PROFILE_RESPONSE,
            groupId = "request-service-profile-response-group",
            containerFactory = "profileResponseKafkaListenerContainerFactory",
            autoStartup = "true"
    )
    public void consume(ProfileResponseEvent event) {

        if (event == null) {
            log.error("REQUEST SERVICE: получено пустое сообщение Kafka");
            return;
        }

        UUID requestId = event.getRequestId();

        log.info(
                "REQUEST SERVICE: получен ответ Kafka, requestId={}",
                requestId
        );

        if (requestId == null) {
            log.error("REQUEST SERVICE: ответ User Service без requestId");
            return;
        }

        CompletableFuture<ProfileResponseEvent> future =
                pendingRequests.remove(requestId);

        if (future == null) {
            log.warn(
                    "REQUEST SERVICE: нет ожидающего запроса для requestId={}",
                    requestId
            );
            return;
        }

        future.complete(event);

        log.info(
                "REQUEST SERVICE: ответ передан ожидающему запросу, requestId={}",
                requestId
        );
    }
}