package ru.servicecompany.user.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.user.kafka.event.MasterAssignmentCandidatesResponseEvent;

@Service
@RequiredArgsConstructor
public class MasterAssignmentCandidatesResponseProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendResponse(
            MasterAssignmentCandidatesResponseEvent event
    ) {
        kafkaTemplate.send(
                KafkaTopics.MASTER_ASSIGNMENT_CANDIDATES_RESPONSE,
                event.getRequestId().toString(),
                event
        );
    }
}