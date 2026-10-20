package ru.servicecompany.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.user.entity.MasterProfile;
import ru.servicecompany.user.kafka.event.MasterByIdRequestEvent;
import ru.servicecompany.user.kafka.event.MasterByIdResponseEvent;
import ru.servicecompany.user.repository.MasterProfileRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class MasterByIdRequestConsumer {

    private final MasterProfileRepository masterProfileRepository;
    private final MasterByIdResponseProducer responseProducer;

    @KafkaListener(
            topics = KafkaTopics.MASTER_BY_ID_REQUEST,
            groupId = "user-service-master-by-id-group",
            containerFactory = "masterByIdRequestKafkaListenerContainerFactory"
    )
    public void consume(MasterByIdRequestEvent event) {

        if (event == null
                || event.getRequestId() == null
                || event.getMasterId() == null) {

            log.error("USER SERVICE: некорректный запрос мастера");
            return;
        }

        try {
            MasterProfile master = masterProfileRepository
                    .findById(event.getMasterId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Мастер не найден")
                    );

            responseProducer.sendResponse(
                    MasterByIdResponseEvent.builder()
                            .requestId(event.getRequestId())
                            .masterId(master.getId())
                            .firstName(master.getFirstName())
                            .lastName(master.getLastName())
                            .middleName(master.getMiddleName())
                            .phone(master.getPhone())
                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "USER SERVICE: не удалось получить мастера, requestId={}, masterId={}",
                    event.getRequestId(),
                    event.getMasterId(),
                    e
            );

            responseProducer.sendResponse(
                    MasterByIdResponseEvent.builder()
                            .requestId(event.getRequestId())
                            .masterId(event.getMasterId())
                            .error("Мастер не найден")
                            .build()
            );
        }
    }
}