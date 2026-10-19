package ru.servicecompany.requestservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.requestservice.kafka.event.EquipmentByIdResponseEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class EquipmentByIdResponseConsumer {

    private final ConcurrentMap<
            UUID,
            CompletableFuture<EquipmentByIdResponseEvent>
            > pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<EquipmentByIdResponseEvent> registerRequest(
            UUID requestId
    ) {
        CompletableFuture<EquipmentByIdResponseEvent> future =
                new CompletableFuture<>();

        pendingRequests.put(requestId, future);

        return future;
    }

    public void removeRequest(UUID requestId) {
        pendingRequests.remove(requestId);
    }

    @KafkaListener(
            id = "equipmentByIdResponseListener",
            topics = KafkaTopics.EQUIPMENT_BY_ID_RESPONSE,
            groupId = "request-service-equipment-by-id-response-group",
            containerFactory = "equipmentResponseKafkaListenerContainerFactory"
    )
    public void consume(EquipmentByIdResponseEvent event) {

        if (event == null || event.getRequestId() == null) {
            log.error("REQUEST SERVICE: некорректный ответ оборудования по ID");
            return;
        }

        CompletableFuture<EquipmentByIdResponseEvent> future =
                pendingRequests.remove(event.getRequestId());

        if (future == null) {
            log.warn(
                    "REQUEST SERVICE: нет ожидающего запроса оборудования, requestId={}",
                    event.getRequestId()
            );
            return;
        }

        future.complete(event);
    }
}