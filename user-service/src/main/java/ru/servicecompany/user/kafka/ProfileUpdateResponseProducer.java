package ru.servicecompany.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.user.kafka.event.ProfileUpdateResponseEvent;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileUpdateResponseProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendResponse(
            UUID requestId,
            UUID authUserId,
            boolean success,
            String reason
    ) {

        ProfileUpdateResponseEvent event =
                ProfileUpdateResponseEvent.builder()
                        .requestId(requestId)
                        .authUserId(authUserId)
                        .success(success)
                        .reason(reason)
                        .build();

        kafkaTemplate.send(
                KafkaTopics.USER_PROFILE_UPDATE_RESPONSE,
                authUserId.toString(),
                event
        );

        log.info(
                "Kafka -> результат обновления профиля: requestId={}, authUserId={}, success={}",
                requestId,
                authUserId,
                success
        );
    }
}