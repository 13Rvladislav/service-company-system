package ru.servicecompany.requestservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.requestservice.kafka.event.EquipmentResponseEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class EquipmentResponseConsumer {

    private final ConcurrentMap<UUID, CompletableFuture<EquipmentResponseEvent>>
            pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<EquipmentResponseEvent> registerRequest(
            UUID requestId
    ) {
        CompletableFuture<EquipmentResponseEvent> future =
                new CompletableFuture<>();

        pendingRequests.put(requestId, future);

        return future;
    }

    public void removeRequest(UUID requestId) {
        pendingRequests.remove(requestId);
    }

    @KafkaListener(
            topics = EquipmentKafkaTopics.EQUIPMENT_RESPONSE,
            groupId = "request-service-equipment-response-group"
    )
    public void consume(EquipmentResponseEvent event) {

        if (event == null || event.getRequestId() == null) {
            log.error("Получен некорректный ответ Kafka по оборудованию");
            return;
        }

        CompletableFuture<EquipmentResponseEvent> future =
                pendingRequests.remove(event.getRequestId());

        if (future == null) {
            log.warn(
                    "Нет ожидающего запроса оборудования: {}",
                    event.getRequestId()
            );
            return;
        }

        future.complete(event);
    }
}