package ru.servicecompany.equipment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import ru.servicecompany.equipment.kafka.event.EquipmentRequestEvent;
import ru.servicecompany.equipment.kafka.event.EquipmentResponseEvent;
import ru.servicecompany.equipment.service.UserEquipmentService;

import java.util.concurrent.TimeUnit;

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

        log.info(
                "EQUIPMENT SERVICE: получен Kafka-запрос оборудования: {}",
                event
        );

        // Проверяем входное событие
        if (event == null
                || event.getRequestId() == null
                || event.getAuthUserId() == null) {

            log.error(
                    "EQUIPMENT SERVICE: получен некорректный Kafka-запрос оборудования"
            );

            return;
        }

        EquipmentResponseEvent response;

        // Получаем оборудование пользователя
        try {

            log.info(
                    "EQUIPMENT SERVICE: получение оборудования пользователя {}, requestId={}",
                    event.getAuthUserId(),
                    event.getRequestId()
            );

            response = EquipmentResponseEvent.builder()
                    .requestId(event.getRequestId())
                    .authUserId(event.getAuthUserId())
                    .equipment(
                            userEquipmentService.getMyEquipment(
                                    event.getAuthUserId()
                            )
                    )
                    .build();

            log.info(
                    "EQUIPMENT SERVICE: оборудование получено, requestId={}",
                    event.getRequestId()
            );

        } catch (Exception e) {

            log.error(
                    "EQUIPMENT SERVICE: ошибка получения оборудования пользователя {}, requestId={}",
                    event.getAuthUserId(),
                    event.getRequestId(),
                    e
            );

            response = EquipmentResponseEvent.builder()
                    .requestId(event.getRequestId())
                    .authUserId(event.getAuthUserId())
                    .error("Не удалось получить оборудование")
                    .build();
        }

        // Отправляем ответ в Kafka
        try {

            log.info(
                    "EQUIPMENT SERVICE: отправка ответа, requestId={}, topic={}",
                    event.getRequestId(),
                    EquipmentKafkaTopics.EQUIPMENT_RESPONSE
            );

            kafkaTemplate.send(
                    EquipmentKafkaTopics.EQUIPMENT_RESPONSE,
                    event.getAuthUserId().toString(),
                    response
            ).get(10, TimeUnit.SECONDS);

            log.info(
                    "EQUIPMENT SERVICE: ответ успешно отправлен, requestId={}",
                    event.getRequestId()
            );

        } catch (Exception e) {

            log.error(
                    "EQUIPMENT SERVICE: ошибка отправки Kafka-ответа, requestId={}",
                    event.getRequestId(),
                    e
            );

            throw new RuntimeException(
                    "Не удалось отправить Kafka-ответ оборудования",
                    e
            );
        }
    }
}