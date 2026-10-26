
package ru.servicecompany.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.user.kafka.event.MasterAssignmentCandidatesRequestEvent;
import ru.servicecompany.user.kafka.event.MasterAssignmentCandidatesResponseEvent;
import ru.servicecompany.user.repository.MasterProfileRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MasterAssignmentCandidatesRequestConsumer {

    private final MasterProfileRepository masterProfileRepository;
    private final MasterAssignmentCandidatesResponseProducer responseProducer;

    @KafkaListener(
            topics = KafkaTopics.MASTER_ASSIGNMENT_CANDIDATES_REQUEST,
            groupId = "user-service-master-assignment-candidates-group",
            containerFactory = "masterAssignmentCandidatesKafkaListenerContainerFactory"
    )
    public void consume(MasterAssignmentCandidatesRequestEvent event) {

        if (event == null
                || event.getRequestId() == null
                || event.getEquipmentTypeId() == null) {
            log.error("USER SERVICE: некорректный запрос кандидатов-мастеров");
            return;
        }

        try {
            List<UUID> masterIds;

            if (event.getZoneId() == null) {
                masterIds = masterProfileRepository
                        .findManualAssignmentCandidates(
                                event.getEquipmentTypeId()
                        );
            } else {
                masterIds = masterProfileRepository
                        .findAssignmentCandidates(
                                event.getZoneId(),
                                event.getEquipmentTypeId()
                        );
            }

            responseProducer.sendResponse(
                    MasterAssignmentCandidatesResponseEvent.builder()
                            .requestId(event.getRequestId())
                            .masterIds(masterIds)
                            .build()
            );

        } catch (Exception e) {
            log.error(
                    "USER SERVICE: ошибка поиска мастеров-кандидатов, requestId={}",
                    event.getRequestId(),
                    e
            );

            responseProducer.sendResponse(
                    MasterAssignmentCandidatesResponseEvent.builder()
                            .requestId(event.getRequestId())
                            .masterIds(List.of())
                            .error("Не удалось получить список мастеров")
                            .build()
            );
        }
    }
}
