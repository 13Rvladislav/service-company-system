package ru.servicecompany.auth.kafka;

import org.springframework.stereotype.Component;
import ru.servicecompany.auth.kafka.event.ProfileUpdateResponseEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ProfileUpdateRequestManager {

    private final ConcurrentMap<
            UUID,
            CompletableFuture<ProfileUpdateResponseEvent>
            > requests = new ConcurrentHashMap<>();

    public CompletableFuture<ProfileUpdateResponseEvent> register(
            UUID requestId
    ) {

        CompletableFuture<ProfileUpdateResponseEvent> future =
                new CompletableFuture<>();

        requests.put(requestId, future);

        return future;
    }

    public void complete(
            UUID requestId,
            ProfileUpdateResponseEvent response
    ) {

        CompletableFuture<ProfileUpdateResponseEvent> future =
                requests.get(requestId);

        if (future != null) {
            future.complete(response);
        }
    }

    public void fail(
            UUID requestId,
            Exception exception
    ) {

        CompletableFuture<ProfileUpdateResponseEvent> future =
                requests.get(requestId);

        if (future != null) {
            future.completeExceptionally(exception);
        }
    }

    public void remove(UUID requestId) {
        requests.remove(requestId);
    }
}