package ru.servicecompany.auth.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.auth.kafka.event.ProfileRequestEvent;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProfileRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Topic для запроса профильных данных.
     */
    private static final String TOPIC =
            "user.profile.request";

    /**
     * Запросить профиль пользователя
     * из user-service.
     */
    public void requestProfile(
            UUID requestId,
            UUID authUserId,
            String role
    ) {

        ProfileRequestEvent event =
                ProfileRequestEvent.builder()
                        .requestId(requestId)
                        .authUserId(authUserId)
                        .role(role)
                        .build();

        kafkaTemplate.send(
                TOPIC,
                authUserId.toString(),
                event
        );

        log.info(
                "Kafka -> запрос профиля отправлен: requestId={}, authUserId={}, role={}",
                requestId,
                authUserId,
                role
        );
    }
}