package ru.servicecompany.requestservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.requestservice.kafka.event.MasterAssignmentCandidatesResponseEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class MasterAssignmentCandidatesResponseConsumer {

    private final ConcurrentMap<
            UUID,
            CompletableFuture<MasterAssignmentCandidatesResponseEvent>
            > pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<MasterAssignmentCandidatesResponseEvent>
    registerRequest(UUID requestId) {

        CompletableFuture<MasterAssignmentCandidatesResponseEvent> future =
                new CompletableFuture<>();

        pendingRequests.put(requestId, future);

        return future;
    }

    public void removeRequest(UUID requestId) {
        pendingRequests.remove(requestId);
    }

    @KafkaListener(
            topics = KafkaTopics.MASTER_ASSIGNMENT_CANDIDATES_RESPONSE,
            groupId = "request-service-master-assignment-candidates-group",
            containerFactory =
                    "masterAssignmentCandidatesResponseKafkaListenerContainerFactory"
    )
    public void consume(MasterAssignmentCandidatesResponseEvent event) {

        if (event == null || event.getRequestId() == null) {
            log.error("Получен некорректный ответ со списком мастеров");
            return;
        }

        CompletableFuture<MasterAssignmentCandidatesResponseEvent> future =
                pendingRequests.remove(event.getRequestId());

        if (future == null) {
            log.warn(
                    "Нет ожидающего запроса кандидатов: requestId={}",
                    event.getRequestId()
            );
            return;
        }

        future.complete(event);
    }
}