package ru.servicecompany.equipment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.servicecompany.equipment.kafka.event.EquipmentRequestEvent;
import ru.servicecompany.equipment.kafka.event.EquipmentResponseEvent;
import ru.servicecompany.equipment.service.UserEquipmentService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EquipmentRequestConsumer {

    private final UserEquipmentService userEquipmentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(
            topics = EquipmentKafkaTopics.EQUIPMENT_REQUEST,
            groupId = "equipment-service-user-equipment-group",
            containerFactory = "equipmentRequestKafkaListenerContainerFactory"
    )
    public void consume(EquipmentRequestEvent event) {

        if (event == null
                || event.getRequestId() == null
                || event.getAuthUserId() == null) {
            log.error("Получен некорректный Kafka-запрос оборудования");
            return;
        }

        EquipmentResponseEvent response;

        try {
            response = EquipmentResponseEvent.builder()
                    .requestId(event.getRequestId())
                    .authUserId(event.getAuthUserId())
                    .equipment(
                            userEquipmentService.getMyEquipment(
                                    event.getAuthUserId()
                            )
                    )
                    .build();

        } catch (Exception e) {
            log.error(
                    "Ошибка получения оборудования пользователя {}",
                    event.getAuthUserId(),
                    e
            );

            response = EquipmentResponseEvent.builder()
                    .requestId(event.getRequestId())
                    .authUserId(event.getAuthUserId())
                    .error("Не удалось получить оборудование")
                    .build();
        }

        kafkaTemplate.send(
                EquipmentKafkaTopics.EQUIPMENT_RESPONSE,
                event.getAuthUserId().toString(),
                response
        );
    }
}