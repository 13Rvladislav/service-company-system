package ru.servicecompany.user.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.user.kafka.event.MasterByIdResponseEvent;

@Service
@RequiredArgsConstructor
public class MasterByIdResponseProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendResponse(MasterByIdResponseEvent event) {

        kafkaTemplate.send(
                KafkaTopics.MASTER_BY_ID_RESPONSE,
                event.getMasterId() == null
                        ? event.getRequestId().toString()
                        : event.getMasterId().toString(),
                event
        );
    }
}