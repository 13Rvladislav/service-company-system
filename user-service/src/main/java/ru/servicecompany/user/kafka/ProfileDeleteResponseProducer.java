package ru.servicecompany.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.user.kafka.event.ProfileDeleteResponseEvent;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileDeleteResponseProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Отправляет результат удаления профиля
     * обратно в auth-service.
     */
    public void sendResponse(
            UUID requestId,
            UUID authUserId,
            boolean success,
            String reason
    ) {

        ProfileDeleteResponseEvent event =
                ProfileDeleteResponseEvent.builder()
                        .requestId(requestId)
                        .authUserId(authUserId)
                        .success(success)
                        .reason(reason)
                        .build();

        kafkaTemplate.send(
                KafkaTopics.USER_PROFILE_DELETE_RESPONSE,
                authUserId.toString(),
                event
        );

        log.info(
                "Kafka -> результат удаления профиля: requestId={}, authUserId={}, success={}",
                requestId,
                authUserId,
                success
        );
    }
}