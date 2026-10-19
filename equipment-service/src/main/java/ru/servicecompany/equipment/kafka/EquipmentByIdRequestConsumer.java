package ru.servicecompany.equipment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.servicecompany.equipment.kafka.event.EquipmentByIdRequestEvent;
import ru.servicecompany.equipment.kafka.event.EquipmentByIdResponseEvent;
import ru.servicecompany.equipment.service.UserEquipmentService;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class EquipmentByIdRequestConsumer {

    private final UserEquipmentService userEquipmentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(
            topics = EquipmentKafkaTopics.EQUIPMENT_BY_ID_REQUEST,
            groupId = "equipment-service-equipment-by-id-group",
            containerFactory = "equipmentByIdRequestKafkaListenerContainerFactory"
    )
    public void consume(EquipmentByIdRequestEvent event) {

        if (event == null
                || event.getRequestId() == null
                || event.getAuthUserId() == null
                || event.getEquipmentId() == null) {

            log.error("EQUIPMENT SERVICE: некорректный запрос оборудования по ID");
            return;
        }

        EquipmentByIdResponseEvent response;

        try {
            var equipment = userEquipmentService.getMyEquipmentById(
                    event.getAuthUserId(),
                    event.getEquipmentId()
            );

            response = EquipmentByIdResponseEvent.builder()
                    .requestId(event.getRequestId())
                    .authUserId(event.getAuthUserId())
                    .equipmentId(event.getEquipmentId())
                    .equipment(equipment)
                    .build();

        } catch (Exception e) {
            log.error(
                    "EQUIPMENT SERVICE: ошибка получения оборудования {}, requestId={}",
                    event.getEquipmentId(),
                    event.getRequestId(),
                    e
            );

            response = EquipmentByIdResponseEvent.builder()
                    .requestId(event.getRequestId())
                    .authUserId(event.getAuthUserId())
                    .equipmentId(event.getEquipmentId())
                    .error("Не удалось получить оборудование")
                    .build();
        }
        log.info(
                "EQUIPMENT SERVICE: topic={}, responseClass={}, requestId={}",
                EquipmentKafkaTopics.EQUIPMENT_BY_ID_RESPONSE,
                response.getClass().getName(),
                event.getRequestId()
        );
        try {
            kafkaTemplate.send(
                    EquipmentKafkaTopics.EQUIPMENT_BY_ID_RESPONSE,
                    event.getAuthUserId().toString(),
                    response
            ).get(10, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error(
                    "EQUIPMENT SERVICE: ошибка отправки ответа по оборудованию, requestId={}",
                    event.getRequestId(),
                    e
            );

            throw new RuntimeException(
                    "Не удалось отправить Kafka-ответ оборудования по ID",
                    e
            );
        }
    }
}