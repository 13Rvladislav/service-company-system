package ru.servicecompany.auth.kafka;

import org.springframework.stereotype.Component;
import ru.servicecompany.auth.kafka.event.ProfileDeleteResponseEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ProfileDeleteRequestManager {

    private final ConcurrentMap<
            UUID,
            CompletableFuture<ProfileDeleteResponseEvent>
            > requests = new ConcurrentHashMap<>();

    /**
     * Зарегистрировать ожидающий запрос.
     */
    public CompletableFuture<ProfileDeleteResponseEvent> register(
            UUID requestId
    ) {

        CompletableFuture<ProfileDeleteResponseEvent> future =
                new CompletableFuture<>();

        requests.put(
                requestId,
                future
        );

        return future;
    }

    /**
     * Передать ответ ожидающему HTTP-запросу.
     */
    public void complete(
            UUID requestId,
            ProfileDeleteResponseEvent response
    ) {

        CompletableFuture<ProfileDeleteResponseEvent> future =
                requests.remove(requestId);

        if (future != null) {
            future.complete(response);
        }
    }

    /**
     * Удалить ожидающий запрос.
     */
    public void remove(UUID requestId) {

        requests.remove(requestId);
    }
}