package ru.servicecompany.auth.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.auth.kafka.event.ProfileDeleteRequestEvent;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileDeleteRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Отправляет запрос на удаление профиля
     * в user-service.
     */
    public void deleteProfile(
            UUID requestId,
            UUID authUserId,
            String role
    ) {

        ProfileDeleteRequestEvent event =
                ProfileDeleteRequestEvent.builder()
                        .requestId(requestId)
                        .authUserId(authUserId)
                        .role(role)
                        .build();

        kafkaTemplate.send(
                "user.profile.delete",
                authUserId.toString(),
                event
        );

        log.info(
                "Kafka -> запрос удаления профиля: requestId={}, authUserId={}, role={}",
                requestId,
                authUserId,
                role
        );
    }
}