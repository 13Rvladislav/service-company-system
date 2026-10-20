package ru.servicecompany.requestservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.requestservice.kafka.event.MasterByIdResponseEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class MasterByIdResponseConsumer {

    private final ConcurrentMap<
            UUID,
            CompletableFuture<MasterByIdResponseEvent>
            > pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<MasterByIdResponseEvent> registerRequest(
            UUID requestId
    ) {
        CompletableFuture<MasterByIdResponseEvent> future =
                new CompletableFuture<>();

        pendingRequests.put(requestId, future);

        return future;
    }

    public void removeRequest(UUID requestId) {
        pendingRequests.remove(requestId);
    }

    @KafkaListener(
            topics = KafkaTopics.MASTER_BY_ID_RESPONSE,
            groupId = "request-service-master-by-id-group",
            containerFactory = "profileResponseKafkaListenerContainerFactory"
    )
    public void consume(MasterByIdResponseEvent event) {

        if (event == null || event.getRequestId() == null) {
            log.error(
                    "REQUEST SERVICE: некорректный ответ мастера"
            );
            return;
        }

        CompletableFuture<MasterByIdResponseEvent> future =
                pendingRequests.remove(event.getRequestId());

        if (future == null) {
            log.warn(
                    "REQUEST SERVICE: нет ожидающего запроса мастера, requestId={}",
                    event.getRequestId()
            );
            return;
        }

        future.complete(event);
    }
}