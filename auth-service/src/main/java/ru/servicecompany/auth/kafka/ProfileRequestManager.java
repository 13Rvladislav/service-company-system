package ru.servicecompany.auth.kafka;

import org.springframework.stereotype.Component;
import ru.servicecompany.auth.kafka.event.ProfileResponseEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ProfileRequestManager {

    private final ConcurrentMap<
            UUID,
            CompletableFuture<ProfileResponseEvent>
            > requests = new ConcurrentHashMap<>();

    /**
     * Регистрирует ожидающий запрос.
     */
    public CompletableFuture<ProfileResponseEvent> register(
            UUID requestId
    ) {

        CompletableFuture<ProfileResponseEvent> future =
                new CompletableFuture<>();

        requests.put(
                requestId,
                future
        );

        return future;
    }

    /**
     * Передаёт полученный Kafka response
     * ожидающему HTTP-запросу.
     */
    public void complete(
            UUID requestId,
            ProfileResponseEvent response
    ) {

        CompletableFuture<ProfileResponseEvent> future =
                requests.remove(requestId);

        if (future != null) {
            future.complete(response);
        }
    }

    /**
     * Завершает запрос с ошибкой.
     */
    public void fail(
            UUID requestId,
            Throwable exception
    ) {

        CompletableFuture<ProfileResponseEvent> future =
                requests.remove(requestId);

        if (future != null) {
            future.completeExceptionally(exception);
        }
    }

    /**
     * Удаляет запрос после timeout.
     */
    public void remove(UUID requestId) {

        requests.remove(requestId);
    }
}