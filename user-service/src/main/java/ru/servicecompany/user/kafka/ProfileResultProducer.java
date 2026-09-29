package ru.servicecompany.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.user.kafka.event.ProfileFailedEvent;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileResultProducer {

    private static final String PROFILE_FAILED_TOPIC =
            "user.profile.failed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Сообщает auth-service,
     * что профиль не удалось создать.
     */
    public void profileFailed(
            UUID authUserId,
            String reason
    ) {

        ProfileFailedEvent event =
                new ProfileFailedEvent(
                        authUserId,
                        reason
                );

        kafkaTemplate.send(
                PROFILE_FAILED_TOPIC,
                authUserId.toString(),
                event
        );

        log.error(
                "Kafka -> профиль НЕ создан: authUserId={}, reason={}",
                authUserId,
                reason
        );
    }
}