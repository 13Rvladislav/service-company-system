package ru.servicecompany.requestservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.kafka.event.MasterAssignmentCandidatesRequestEvent;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MasterAssignmentCandidatesRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void requestCandidates(
            UUID requestId,
            UUID zoneId,
            UUID equipmentTypeId
    ) {
        MasterAssignmentCandidatesRequestEvent event =
                MasterAssignmentCandidatesRequestEvent.builder()
                        .requestId(requestId)
                        .zoneId(zoneId)
                        .equipmentTypeId(equipmentTypeId)
                        .build();

        kafkaTemplate.send(
                KafkaTopics.MASTER_ASSIGNMENT_CANDIDATES_REQUEST,
                requestId.toString(),
                event
        );

        log.info(
                "Отправлен запрос кандидатов мастеров: requestId={}, zoneId={}, equipmentTypeId={}",
                requestId,
                zoneId,
                equipmentTypeId
        );
    }
}