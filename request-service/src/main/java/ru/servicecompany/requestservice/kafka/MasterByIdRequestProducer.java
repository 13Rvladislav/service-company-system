package ru.servicecompany.requestservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.kafka.event.MasterByIdRequestEvent;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MasterByIdRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void requestMaster(
            UUID requestId,
            UUID masterId
    ) {
        requestMaster(requestId, masterId, null);
    }

    public void requestMaster(
            UUID requestId,
            UUID masterId,
            UUID equipmentTypeId
    ) {
        MasterByIdRequestEvent event =
                MasterByIdRequestEvent.builder()
                        .requestId(requestId)
                        .masterId(masterId)
                        .equipmentTypeId(equipmentTypeId)
                        .build();

        kafkaTemplate.send(
                KafkaTopics.MASTER_BY_ID_REQUEST,
                masterId.toString(),
                event
        );

        log.info(
                "REQUEST SERVICE: отправлен запрос мастера, "
                        + "requestId={}, masterId={}, equipmentTypeId={}",
                requestId,
                masterId,
                equipmentTypeId
        );
    }
}