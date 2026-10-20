package ru.servicecompany.requestservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.requestservice.kafka.event.AddressByIdResponseEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class AddressByIdResponseConsumer {

    private final ConcurrentMap<
            UUID,
            CompletableFuture<AddressByIdResponseEvent>
            > pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<AddressByIdResponseEvent> registerRequest(
            UUID requestId
    ) {
        CompletableFuture<AddressByIdResponseEvent> future =
                new CompletableFuture<>();

        pendingRequests.put(requestId, future);

        return future;
    }

    public void removeRequest(UUID requestId) {
        pendingRequests.remove(requestId);
    }

    @KafkaListener(
            topics = KafkaTopics.ADDRESS_BY_ID_RESPONSE,
            groupId = "request-service-address-by-id-group",
            containerFactory = "addressByIdResponseKafkaListenerContainerFactory"
    )
    public void consume(AddressByIdResponseEvent event) {

        if (event == null || event.getRequestId() == null) {
            log.error("REQUEST SERVICE: некорректный ответ адреса");
            return;
        }

        CompletableFuture<AddressByIdResponseEvent> future =
                pendingRequests.remove(event.getRequestId());

        if (future == null) {
            log.warn(
                    "REQUEST SERVICE: нет ожидающего запроса адреса, requestId={}",
                    event.getRequestId()
            );
            return;
        }

        future.complete(event);
    }
}