package ru.servicecompany.requestservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.kafka.event.EquipmentRequestEvent;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipmentRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void requestEquipment(
            UUID requestId,
            UUID authUserId
    ) {
        EquipmentRequestEvent event =
                EquipmentRequestEvent.builder()
                        .requestId(requestId)
                        .authUserId(authUserId)
                        .build();

        kafkaTemplate.send(
                EquipmentKafkaTopics.EQUIPMENT_REQUEST,
                authUserId.toString(),
                event
        );
    }
}