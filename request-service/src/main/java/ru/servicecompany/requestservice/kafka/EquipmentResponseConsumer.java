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

        log.info(
                "REQUEST SERVICE: зарегистрирован запрос оборудования, requestId={}",
                requestId
        );

        return future;
    }

    public void removeRequest(UUID requestId) {
        pendingRequests.remove(requestId);
    }

    @KafkaListener(
            id = "equipmentResponseListener",
            topics = EquipmentKafkaTopics.EQUIPMENT_RESPONSE,
            groupId = "request-service-equipment-response-group",
            containerFactory = "equipmentResponseKafkaListenerContainerFactory"
    )
    public void consume(EquipmentResponseEvent event) {

        if (event == null) {
            log.error(
                    "REQUEST SERVICE: получен пустой ответ оборудования"
            );
            return;
        }

        UUID requestId = event.getRequestId();

        if (requestId == null) {
            log.error(
                    "REQUEST SERVICE: ответ оборудования без requestId"
            );
            return;
        }

        log.info(
                "REQUEST SERVICE: получен ответ оборудования, requestId={}",
                requestId
        );

        CompletableFuture<EquipmentResponseEvent> future =
                pendingRequests.remove(requestId);

        if (future == null) {
            log.warn(
                    "REQUEST SERVICE: нет ожидающего запроса оборудования, requestId={}",
                    requestId
            );
            return;
        }

        future.complete(event);

        log.info(
                "REQUEST SERVICE: ответ оборудования передан ожидающему запросу, requestId={}",
                requestId
        );
    }
}