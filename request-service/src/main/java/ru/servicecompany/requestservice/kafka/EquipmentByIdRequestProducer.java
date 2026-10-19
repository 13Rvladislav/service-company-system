package ru.servicecompany.requestservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.kafka.event.EquipmentByIdRequestEvent;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipmentByIdRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void requestEquipmentById(
            UUID requestId,
            UUID authUserId,
            UUID equipmentId
    ) {
        EquipmentByIdRequestEvent event =
                EquipmentByIdRequestEvent.builder()
                        .requestId(requestId)
                        .authUserId(authUserId)
                        .equipmentId(equipmentId)
                        .build();

        kafkaTemplate.send(
                KafkaTopics.EQUIPMENT_BY_ID_REQUEST,
                authUserId.toString(),
                event
        );

    }
}